# Guardian Documentation System

Guardian is an AI-powered aftermarket automotive co-driver for mid-range and older vehicles. This repository is the design foundation before implementation: it defines product, vehicle-edge, mobile, cloud, AI, privacy, test, manufacturing, and business decisions.

## How to use this documentation

1. Start with [Master Roadmap](00_Master_Roadmap/master-roadmap.md), [Vision](01_Product/vision.md), and [MVP Scope](01_Product/mvp-scope.md).
2. Treat every document's TODOs as a research and decision backlog; convert approved items into owned work.
3. Update the version history in the document that changed, and log cross-cutting decisions in [Decision Log](00_Master_Roadmap/decision-log.md).
4. Do not treat these drafts as safety, legal, certification, or regulatory advice; engage qualified specialists before production release.

## Documentation map

| Area | Focus |
|---|---|
| 0004 | Strategy, research, architecture, and feature behavior |
| 0511 | Hardware, AI, mobile, backend, data, APIs, and firmware |
| 1216 | Experience, security, testing, deployment, and manufacturing |
| 1720 | Business, research-paper, investor, and project-management materials |

## Non-negotiable MVP guardrails

- Read-only vehicle integration; no direct vehicle control.
- Driver safety and non-distraction take precedence over assistant usefulness.
- Explicit consent, data minimization, encryption, and recoverable updates.
- Plug-and-play design aligned to the India-first INR 8,000-INR 10,000 retail target.
