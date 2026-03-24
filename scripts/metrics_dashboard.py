#!/usr/bin/env python3
"""Build historical execution metrics and a static dashboard.

Inputs:
- Surefire XML reports from target/surefire-reports/*.xml
- Optional previous metrics from gh-pages checkout

Outputs:
- metrics/current/run.json
- metrics/publish/metrics/<branch>/runs/<run_id>.json
- metrics/publish/metrics/<branch>/index.json
- metrics/publish/index.html
"""

from __future__ import annotations

import argparse
import glob
import json
import os
from collections import Counter, defaultdict
from datetime import datetime, timezone
from typing import Any
import xml.etree.ElementTree as ET


DASHBOARD_HTML = """<!doctype html>
<html lang=\"pt-BR\">
<head>
  <meta charset=\"utf-8\" />
  <meta name=\"viewport\" content=\"width=device-width,initial-scale=1\" />
  <title>Dashboard de Execucao</title>
  <script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>
  <style>
    :root {
      --bg: #f3f5f9;
      --card: #ffffff;
      --text: #1a2433;
      --muted: #5b6b82;
      --accent: #0a7d65;
      --accent-2: #1565c0;
      --danger: #c62828;
    }
    * { box-sizing: border-box; }
    body {
      margin: 0;
      padding: 24px;
      font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
      background: linear-gradient(160deg, #eef2f8 0%, #f8fafc 100%);
      color: var(--text);
    }
    .container { max-width: 1200px; margin: 0 auto; }
    h1 { margin: 0 0 6px; font-size: 28px; }
    .subtitle { color: var(--muted); margin-bottom: 18px; }
    .cards {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: 12px;
      margin-bottom: 16px;
    }
    .card {
      background: var(--card);
      border-radius: 12px;
      padding: 14px;
      box-shadow: 0 4px 16px rgba(0,0,0,0.08);
    }
    .label { color: var(--muted); font-size: 13px; margin-bottom: 8px; }
    .value { font-size: 24px; font-weight: 700; }
    .value.pass { color: var(--accent); }
    .value.flaky { color: var(--danger); }
    .value.time { color: var(--accent-2); }
    .panel {
      background: var(--card);
      border-radius: 12px;
      padding: 14px;
      box-shadow: 0 4px 16px rgba(0,0,0,0.08);
      margin-bottom: 14px;
    }
    ul { margin: 8px 0 0; padding-left: 18px; }
    li { margin-bottom: 6px; }
  </style>
</head>
<body>
  <div class=\"container\">
    <h1>Dashboard Historico de Execucao</h1>
    <div class=\"subtitle\" id=\"meta\">Carregando metricas...</div>

    <section class=\"cards\">
      <div class=\"card\">
        <div class=\"label\">Pass rate atual</div>
        <div class=\"value pass\" id=\"passRate\">-</div>
      </div>
      <div class=\"card\">
        <div class=\"label\">Flaky rate (janela)</div>
        <div class=\"value flaky\" id=\"flakyRate\">-</div>
      </div>
      <div class=\"card\">
        <div class=\"label\">Duracao atual</div>
        <div class=\"value time\" id=\"duration\">-</div>
      </div>
      <div class=\"card\">
        <div class=\"label\">Execucoes na janela</div>
        <div class=\"value\" id=\"window\">-</div>
      </div>
    </section>

    <section class=\"panel\">
      <h3>Tendencia (pass rate e duracao)</h3>
      <canvas id=\"trendChart\" height=\"100\"></canvas>
    </section>

    <section class=\"panel\">
      <h3>Top falhas da execucao atual</h3>
      <ul id=\"topFailures\"></ul>
    </section>

    <section class=\"panel\">
      <h3>Falhas recorrentes na janela</h3>
      <canvas id=\"recurrentChart\" height=\"90\"></canvas>
      <ul id=\"recurrentFailures\"></ul>
    </section>
  </div>

  <script>
    function detectBranch() {
      const p = window.location.pathname.toLowerCase();
      return p.includes('/hml/') ? 'hml' : 'main';
    }

    function toPercent(v) {
      return (Number(v || 0) * 100).toFixed(2) + '%';
    }

    function toSeconds(v) {
      return Number(v || 0).toFixed(1) + 's';
    }

    async function loadMetrics() {
      const branch = detectBranch();
      const resp = await fetch(`metrics/${branch}/index.json`, { cache: 'no-store' });
      if (!resp.ok) throw new Error('Falha ao carregar index de metricas');
      return resp.json();
    }

    function render(data) {
      const current = data.current || {};
      const trend = data.trend || [];
      const top = current.top_failures || [];
      const recurrent = data.top_recurrent_failures || [];

      document.getElementById('meta').textContent =
        `Branch: ${data.branch || '-'} | Run: ${current.run_number || '-'} | Commit: ${(current.sha || '').slice(0, 7)}`;
      document.getElementById('passRate').textContent = toPercent(current.pass_rate);
      document.getElementById('flakyRate').textContent = toPercent(data.flaky_rate);
      document.getElementById('duration').textContent = toSeconds(current.duration_seconds);
      document.getElementById('window').textContent = String(data.runs_count || 0);

      const ul = document.getElementById('topFailures');
      if (!top.length) {
        ul.innerHTML = '<li>Nenhuma falha na execucao atual.</li>';
      } else {
        ul.innerHTML = top.map(x => `<li>${x.test} (${x.count})</li>`).join('');
      }

      const recurrentUl = document.getElementById('recurrentFailures');
      if (!recurrent.length) {
        recurrentUl.innerHTML = '<li>Nenhuma falha recorrente na janela.</li>';
      } else {
        recurrentUl.innerHTML = recurrent
          .map(x => `<li>${x.test} (${x.fail_runs}/${x.total_runs} runs com falha, taxa ${(Number(x.failure_rate || 0) * 100).toFixed(1)}%)</li>`)
          .join('');
      }

      const ctx = document.getElementById('trendChart');
      new Chart(ctx, {
        type: 'line',
        data: {
          labels: trend.map(t => `#${t.run_number || '?'}`),
          datasets: [
            {
              label: 'Pass rate (%)',
              data: trend.map(t => Number(t.pass_rate || 0) * 100),
              borderColor: '#0a7d65',
              backgroundColor: 'rgba(10,125,101,0.15)',
              yAxisID: 'y',
              tension: 0.2
            },
            {
              label: 'Duracao (s)',
              data: trend.map(t => Number(t.duration_seconds || 0)),
              borderColor: '#1565c0',
              backgroundColor: 'rgba(21,101,192,0.15)',
              yAxisID: 'y1',
              tension: 0.2
            }
          ]
        },
        options: {
          responsive: true,
          interaction: { mode: 'index', intersect: false },
          scales: {
            y: { position: 'left', suggestedMin: 0, suggestedMax: 100 },
            y1: { position: 'right', grid: { drawOnChartArea: false }, suggestedMin: 0 }
          }
        }
      });

      const rctx = document.getElementById('recurrentChart');
      new Chart(rctx, {
        type: 'bar',
        data: {
          labels: recurrent.map(x => x.test.length > 45 ? x.test.slice(0, 45) + '...' : x.test),
          datasets: [
            {
              label: 'Taxa de falha recorrente (%)',
              data: recurrent.map(x => Number(x.failure_rate || 0) * 100),
              borderColor: '#c62828',
              backgroundColor: 'rgba(198,40,40,0.35)'
            }
          ]
        },
        options: {
          responsive: true,
          scales: {
            y: { suggestedMin: 0, suggestedMax: 100 }
          },
          plugins: {
            legend: { display: true }
          }
        }
      });
    }

    loadMetrics().then(render).catch(err => {
      document.getElementById('meta').textContent = 'Erro ao carregar dashboard: ' + err.message;
    });
  </script>
</body>
</html>
"""


def parse_surefire_reports(glob_pattern: str) -> dict[str, Any]:
    report_files = sorted(glob.glob(glob_pattern))
    if not report_files:
        raise RuntimeError("Nenhum arquivo surefire encontrado para gerar metricas.")

    total = failures = errors = skipped = 0
    duration_seconds = 0.0
    failed_occurrences: Counter[str] = Counter()
    failed_tests: set[str] = set()
    all_tests: set[str] = set()

    for path in report_files:
        try:
            root = ET.parse(path).getroot()
        except ET.ParseError:
            continue

        suites = [root] if root.tag == "testsuite" else root.findall("testsuite")
        for suite in suites:
            total += int(suite.attrib.get("tests", 0))
            failures += int(suite.attrib.get("failures", 0))
            errors += int(suite.attrib.get("errors", 0))
            skipped += int(suite.attrib.get("skipped", 0))
            duration_seconds += float(suite.attrib.get("time", 0.0))

            for case in suite.findall("testcase"):
                name = f"{case.attrib.get('classname', '?')}#{case.attrib.get('name', '?')}"
                all_tests.add(name)
                if case.find("failure") is not None or case.find("error") is not None:
                    failed_occurrences[name] += 1
                    failed_tests.add(name)

    executed = max(total - skipped, 0)
    passed = max(executed - failures - errors, 0)
    pass_rate = (passed / executed) if executed > 0 else 0.0

    return {
        "totals": {
            "total": total,
            "executed": executed,
            "passed": passed,
            "failed": failures + errors,
            "skipped": skipped,
        },
        "pass_rate": pass_rate,
        "duration_seconds": duration_seconds,
        "top_failures": [
            {"test": test_name, "count": count}
            for test_name, count in failed_occurrences.most_common(10)
        ],
        "failed_tests": sorted(failed_tests),
        "all_tests": sorted(all_tests),
    }


def read_json_file(path: str) -> dict[str, Any] | None:
    try:
        with open(path, "r", encoding="utf-8") as handle:
            return json.load(handle)
    except Exception:
        return None


def write_json_file(path: str, data: dict[str, Any]) -> None:
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as handle:
        json.dump(data, handle, ensure_ascii=False, indent=2)


def build_run_snapshot(args: argparse.Namespace) -> dict[str, Any]:
    metrics = parse_surefire_reports(args.surefire_glob)
    run_snapshot = {
        "branch": args.branch,
        "run_id": args.run_id,
        "run_number": args.run_number,
        "sha": args.sha,
        "timestamp": args.timestamp,
        **metrics,
    }
    return run_snapshot


def load_historical_runs(history_runs_dir: str) -> list[dict[str, Any]]:
    if not os.path.isdir(history_runs_dir):
        return []
    runs: list[dict[str, Any]] = []
    for path in glob.glob(os.path.join(history_runs_dir, "*.json")):
        loaded = read_json_file(path)
        if isinstance(loaded, dict):
            runs.append(loaded)
    return runs


def compute_flaky_rate(runs: list[dict[str, Any]]) -> tuple[float, int]:
    status_by_test: defaultdict[str, set[str]] = defaultdict(set)
    for run in runs:
        failed = set(run.get("failed_tests", []))
        all_tests = set(run.get("all_tests", []))
        for test_name in all_tests:
            status_by_test[test_name].add("failed" if test_name in failed else "passed")

    if not status_by_test:
        return 0.0, 0

    flaky_tests_count = sum(
        1
        for statuses in status_by_test.values()
        if "failed" in statuses and "passed" in statuses
    )
    return flaky_tests_count / len(status_by_test), flaky_tests_count


def compute_top_recurrent_failures(runs: list[dict[str, Any]], limit: int = 10) -> list[dict[str, Any]]:
    if not runs:
        return []

    stats: defaultdict[str, dict[str, int]] = defaultdict(lambda: {"fail_runs": 0, "total_runs": 0})

    for run in runs:
        failed = set(run.get("failed_tests", []))
        all_tests = set(run.get("all_tests", []))
        for test_name in all_tests:
            stats[test_name]["total_runs"] += 1
            if test_name in failed:
                stats[test_name]["fail_runs"] += 1

    recurrent: list[dict[str, Any]] = []
    for test_name, values in stats.items():
        total_runs = values["total_runs"]
        fail_runs = values["fail_runs"]
        if fail_runs <= 1:
            continue
        failure_rate = (fail_runs / total_runs) if total_runs else 0.0
        recurrent.append(
            {
                "test": test_name,
                "fail_runs": fail_runs,
                "total_runs": total_runs,
                "failure_rate": failure_rate,
            }
        )

    recurrent.sort(key=lambda item: (item["fail_runs"], item["failure_rate"]), reverse=True)
    return recurrent[: max(limit, 1)]


def safe_run_number(run: dict[str, Any]) -> int:
    try:
        return int(str(run.get("run_number", "0")))
    except ValueError:
        return 0


def build_index(branch: str, window: int, runs: list[dict[str, Any]], current: dict[str, Any]) -> dict[str, Any]:
    flaky_rate, flaky_tests_count = compute_flaky_rate(runs)
    top_recurrent_failures = compute_top_recurrent_failures(runs)
    trend = [
        {
            "run_id": run.get("run_id"),
            "run_number": run.get("run_number"),
            "timestamp": run.get("timestamp"),
            "pass_rate": run.get("pass_rate", 0.0),
            "duration_seconds": run.get("duration_seconds", 0.0),
            "failed": run.get("totals", {}).get("failed", 0),
        }
        for run in runs
    ]
    return {
        "branch": branch,
        "window": window,
        "runs_count": len(runs),
        "current": current,
        "flaky_rate": flaky_rate,
        "flaky_tests_count": flaky_tests_count,
        "top_recurrent_failures": top_recurrent_failures,
        "trend": trend,
    }


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Build CI metrics history and dashboard.")
    parser.add_argument("--branch", required=True)
    parser.add_argument("--run-id", required=True)
    parser.add_argument("--run-number", required=True)
    parser.add_argument("--sha", required=True)
    parser.add_argument("--timestamp", default=datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"))
    parser.add_argument("--window", type=int, default=100)
    parser.add_argument("--surefire-glob", default="target/surefire-reports/*.xml")
    parser.add_argument("--history-root", default="gh-pages-history/metrics")
    parser.add_argument("--current-out", default="metrics/current/run.json")
    parser.add_argument("--publish-root", default="metrics/publish")
    return parser.parse_args()


def main() -> int:
    args = parse_args()

    run_snapshot = build_run_snapshot(args)
    write_json_file(args.current_out, run_snapshot)

    history_runs_dir = os.path.join(args.history_root, args.branch, "runs")
    historical_runs = load_historical_runs(history_runs_dir)
    historical_runs = [
        run
        for run in historical_runs
        if str(run.get("run_id", "")) != str(args.run_id)
    ]
    historical_runs.append(run_snapshot)
    historical_runs.sort(key=safe_run_number)
    historical_runs = historical_runs[-max(args.window, 1) :]

    runs_out_dir = os.path.join(args.publish_root, "metrics", args.branch, "runs")
    for run in historical_runs:
        run_id = str(run.get("run_id", "unknown"))
        write_json_file(os.path.join(runs_out_dir, f"{run_id}.json"), run)

    index = build_index(args.branch, args.window, historical_runs, run_snapshot)
    index_out = os.path.join(args.publish_root, "metrics", args.branch, "index.json")
    write_json_file(index_out, index)

    dashboard_out = os.path.join(args.publish_root, "index.html")
    os.makedirs(os.path.dirname(dashboard_out), exist_ok=True)
    with open(dashboard_out, "w", encoding="utf-8") as handle:
        handle.write(DASHBOARD_HTML)

    print(f"Metrics dashboard generated for branch '{args.branch}'.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
