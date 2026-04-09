"use client";

import { useRouter } from "next/navigation";
import { useState, type FormEvent } from "react";

import { login } from "../../lib/api/auth";
import { routes } from "../../lib/constants/routes";

export function LoginForm() {
  const router = useRouter();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setIsSubmitting(true);

    try {
      await login({ username, password });
      router.replace(routes.dashboard);
      router.refresh();
    } catch (submissionError) {
      setError(
        submissionError instanceof Error
          ? submissionError.message
          : "Unable to sign you in right now.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form className="login-form" onSubmit={handleSubmit}>
      <div className="login-form-header">
        <h1>Member Login</h1>
        <p>Sign in to manage handbook users, workflow queues, and published tax content.</p>
      </div>

      <label className="login-field">
        <span>Email or username</span>
        <input
          autoComplete="username"
          onChange={(event) => setUsername(event.target.value)}
          placeholder="admin"
          required
          type="text"
          value={username}
        />
      </label>

      <label className="login-field">
        <span>Password</span>
        <input
          autoComplete="current-password"
          onChange={(event) => setPassword(event.target.value)}
          placeholder="Enter your password"
          required
          type="password"
          value={password}
        />
      </label>

      {error ? <div className="login-error">{error}</div> : null}

      <button className="login-submit" disabled={isSubmitting} type="submit">
        {isSubmitting ? "Signing in..." : "Login"}
      </button>

      <button className="login-link" type="button">
        Forgot Username / Password?
      </button>
    </form>
  );
}
