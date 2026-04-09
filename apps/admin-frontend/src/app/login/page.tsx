import { LoginForm } from "../../components/auth/LoginForm";

export default function LoginPage() {
  return (
    <main className="login-page">
      <section className="login-shell">
        <div className="login-illustration">
          <span className="floating-shape floating-shape-blue" />
          <span className="floating-shape floating-shape-green" />
          <span className="floating-shape floating-shape-orange" />
          <span className="floating-shape floating-shape-square" />
          <div className="login-orb">
            <div className="login-device">
              <div className="device-screen">
                <div className="device-avatar" />
                <div className="device-avatar device-avatar-base" />
              </div>
              <div className="device-stand" />
            </div>
          </div>
          <div className="login-copy">
            <span className="eyebrow">RRA Tax Handbook Platform</span>
            <h2>Secure, multilingual administration for trusted tax content.</h2>
            <p>
              Keep users, review queues, and publishing decisions in one clean workspace
              built around RRA workflows.
            </p>
          </div>
        </div>

        <div className="login-panel">
          <LoginForm />
        </div>
      </section>
    </main>
  );
}
