## Pre-release Regression Checklist

1. **Authentication & Roles**
   - Sign in with resident, doctor, and admin accounts via `/auth/login`.
   - Switch roles and confirm navigation menus and protected routes match permissions.

2. **Resident Experience**
   - Update a health profile, refresh, and verify the persisted data.
   - Create health records, paginate the list, and inspect the trend chart.
   - Create, toggle, and delete reminders and guidance plans.

3. **Doctor & Appointment Flow**
   - Search doctors with department, keyword, and availability filters.
   - Book, stress test (concurrent booking), and cancel appointments; confirm slot capacity.
   - Start an online consult, exchange messages, and generate a prescription visible to both parties.

4. **Content & Community**
   - Draft, submit, approve, publish, and unpublish an article; confirm the public feed updates.
   - Publish an activity, enroll or cancel as a resident, and verify the "My Activities" list.

5. **Admin Console**
   - Execute CRUD for users and doctors, including enable/disable and tag maintenance.
   - Load health, abnormal-rate, and appointment charts on the analytics dashboard.
   - Switch audit sources (auth, appointment, activity) and confirm pagination.

6. **Infrastructure & Integrations**
   - Gateway blocks unauthenticated requests and forwards JWT claims when present.
   - Redis-backed appointment slots remain accurate under concurrent bookings.
   - RocketMQ optional: no config, no errors; with config, observe events for appointments, content, and activities.
