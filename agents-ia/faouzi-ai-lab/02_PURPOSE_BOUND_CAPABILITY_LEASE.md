# Purpose-Bound Capability Lease

**Working name:** `purpose-lease`

> Give an AI agent temporary permissions that automatically expire when its goal changes.

## Problem

Most agent permission systems ask: **Can this agent call this tool?** A stronger question is: **Can it call this tool for this exact purpose?**

A permission granted to summarize an invoice should not remain valid when the active purpose becomes emailing the customer.

## Core idea

A capability lease is bound to a normalized goal fingerprint, tools, resources, maximum calls, budget, expiration and effect classes.

```json
{
  "lease_id": "lease_9f4a",
  "goal_hash": "sha256:81ab...",
  "tools": ["drive.read", "pdf.extract"],
  "resources": ["drive:/Invoices/2026/**"],
  "effects": ["READ"],
  "max_calls": 12,
  "max_cost_usd": 0.20
}
```

If the purpose changes materially the lease becomes `SUSPENDED_PENDING_REBIND`.

```text
WHO + WHAT + WHERE + WHEN + WHY
```

The **WHY** is the differentiating boundary.

## Example

```text
DENY
reason: purpose changed
old purpose: summarize invoice
new purpose: contact customer
action: request new lease
```

## Integration

```text
Intent Lock
    ↓
Purpose Lease
    ↓
Effect Manifest
    ↓
Tool execution
```

> **Permissions should expire when the purpose expires.**