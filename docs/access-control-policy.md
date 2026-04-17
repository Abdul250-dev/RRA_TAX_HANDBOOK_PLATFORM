# RRA Tax Handbook Platform Access Control Policy

## Purpose

This document defines the official role model for the RRA Tax Handbook Platform. It is intended to support a controlled publishing workflow, strong internal governance, and clear separation between content operations and platform administration.

The platform follows two core principles:

- only verified RRA employees may become authenticated system users
- roles and permissions are defined and managed by this platform, not by the external employee source

The external RRA employee source is used only to verify eligibility. Authorization remains the responsibility of this system.

## Role Set

### `PUBLIC`

Anonymous access level for non-authenticated users.

Users in this access level can:

- view published articles
- search public content
- download public documents
- read FAQs

Notes:

- `PUBLIC` does not require login
- `PUBLIC` is part of the access-control model, but it should not normally be assigned as an authenticated staff role

### `EDITOR`

Content creation role.

Users with this role can:

- create articles
- edit draft content
- upload documents
- create FAQs

Users with this role cannot:

- publish content
- change platform settings
- manage users

### `REVIEWER`

Content quality and validation role.

Users with this role can:

- review submitted content
- request changes
- approve content for publishing readiness

Users with this role cannot:

- create users
- assign roles
- change system settings

### `PUBLISHER`

Final publication control role.

Users with this role can:

- publish and unpublish content
- schedule publishing
- archive content

This role is important for government-grade editorial control because it separates approval from final release authority.

### `ADMIN`

Operational administration role.

Users with this role can:

- manage users
- assign roles
- manage categories and operational configuration
- access all content areas
- override workflow steps when required
- manage system configuration
- manage security-sensitive settings
- access audit oversight functions

### `AUDITOR`

Read-only oversight role.

Users with this role can:

- view audit logs
- review changes
- monitor platform activity for compliance and accountability

Users with this role cannot:

- modify content
- assign users
- change configuration

## Governance Model

### Identity Eligibility

Before a user is created in this platform, the person must first be verified against the RRA employee source. This ensures that non-employees cannot become system users.

### Authorization Ownership

Once a person is verified as an eligible RRA employee, this platform manages:

- the local user account
- role assignment
- access control rules
- workflow permissions

## Current Technical Implementation

The backend currently implements this policy as follows:

- RRA employee eligibility is checked against a local employee directory snapshot until direct external access is available
- roles are stored and seeded locally by the platform
- user creation is blocked unless the employee exists in the verified directory snapshot

## Recommended Operational Notes

- `PUBLIC` should remain an access profile, not a normal assigned staff role
- `ADMIN` is the highest privileged authenticated staff role in the current policy
- `AUDITOR` should remain read-only
- publication authority should remain separate from content creation authority

## Approval Summary

Approved role set:

- `PUBLIC`
- `EDITOR`
- `REVIEWER`
- `PUBLISHER`
- `ADMIN`
- `AUDITOR`
