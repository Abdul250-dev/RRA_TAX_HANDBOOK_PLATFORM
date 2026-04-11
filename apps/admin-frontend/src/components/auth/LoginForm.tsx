'use client';

import { useRouter } from 'next/navigation';
import { useState, type FormEvent } from 'react';

import { login } from '../../lib/api/auth';
import { routes } from '../../lib/constants/routes';

function FieldIcon({ children }: { children: React.ReactNode }) {
  return (
    <span className="pointer-events-none absolute left-0 top-1/2 flex -translate-y-1/2 items-center text-[var(--rra-blue-secondary)]">
      {children}
    </span>
  );
}

export function LoginForm() {
  const router = useRouter();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
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
          : 'Unable to sign you in right now.',
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form className="flex w-full flex-col gap-6" onSubmit={handleSubmit}>
      <div className="relative">
        <FieldIcon>
          <svg
            width="16"
            height="16"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.8"
          >
            <rect x="2" y="4" width="20" height="16" rx="2" />
            <path d="m2 7 10 7 10-7" />
          </svg>
        </FieldIcon>
        <input
          autoComplete="username"
          className="w-full border-0 border-b border-b-[rgba(9,21,76,0.22)] bg-transparent px-0 pb-2.5 pl-9 pt-2.5 text-[0.95rem] text-[var(--rra-blue)] outline-none placeholder:text-[rgba(9,21,76,0.46)] focus:border-b-[var(--rra-orange)]"
          onChange={(e) => setUsername(e.target.value)}
          placeholder="Email ID"
          required
          type="text"
          value={username}
        />
      </div>

      <div className="relative">
        <FieldIcon>
          <svg
            width="16"
            height="16"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.8"
          >
            <rect x="3" y="11" width="18" height="11" rx="2" />
            <path d="M7 11V7a5 5 0 0 1 10 0v4" />
          </svg>
        </FieldIcon>
        <input
          autoComplete="current-password"
          className="w-full border-0 border-b border-b-[rgba(9,21,76,0.22)] bg-transparent px-0 pb-2.5 pl-9 pt-2.5 text-[0.95rem] text-[var(--rra-blue)] outline-none placeholder:text-[rgba(9,21,76,0.46)] focus:border-b-[var(--rra-orange)]"
          onChange={(e) => setPassword(e.target.value)}
          placeholder="Password"
          required
          type="password"
          value={password}
        />
      </div>

      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <label className="flex cursor-pointer items-center gap-2 text-[0.82rem] text-[rgba(9,21,76,0.65)]">
          <input className="accent-[var(--rra-orange)]" type="checkbox" />
          Remember me
        </label>
        <button
          className="self-start border-0 bg-transparent p-0 text-[0.82rem] italic text-[var(--rra-orange)] sm:self-auto"
          type="button"
        >
          Forgot Password?
        </button>
      </div>

      {error ? (
        <p className="m-0 rounded-lg border border-[rgba(255,174,27,0.25)] bg-[rgba(255,174,27,0.12)] px-3.5 py-2.5 text-center text-[0.85rem] text-[var(--danger)]">
          {error}
        </p>
      ) : null}

      <button
        className="h-12 w-full rounded-md border border-[var(--rra-orange)] bg-[var(--rra-blue)] text-[0.82rem] font-semibold uppercase tracking-[0.22em] text-[var(--rra-white)] transition-colors duration-200 hover:border-[var(--rra-green)] hover:bg-[var(--rra-green)] disabled:cursor-progress disabled:opacity-70"
        disabled={isSubmitting}
        type="submit"
      >
        {isSubmitting ? 'Signing in...' : 'Login'}
      </button>

      <p className="m-0 text-center text-[0.75rem] tracking-[0.04em] text-[rgba(9,21,76,0.42)]">
        Rwanda Revenue Authority · Secure Access
      </p>
    </form>
  );
}
