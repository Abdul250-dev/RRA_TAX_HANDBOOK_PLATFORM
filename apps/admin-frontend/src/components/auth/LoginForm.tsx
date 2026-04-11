'use client';

import { useRouter } from 'next/navigation';
import { useState, type FormEvent } from 'react';

import { login } from '../../lib/api/auth';
import { routes } from '../../lib/constants/routes';

const inputStyle: React.CSSProperties = {
  width: '100%',
  background: 'transparent',
  border: 'none',
  borderBottom: '1px solid rgba(9,21,76,0.22)',
  padding: '10px 0 10px 36px',
  color: '#09154c',
  fontSize: '0.95rem',
  outline: 'none',
  caretColor: '#ffae1b',
};

const iconWrapStyle: React.CSSProperties = {
  position: 'absolute',
  left: 0,
  top: '50%',
  transform: 'translateY(-50%)',
  color: '#5196cf',
  display: 'flex',
  alignItems: 'center',
};

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
    <form
      onSubmit={handleSubmit}
      style={{ width: '100%', display: 'flex', flexDirection: 'column', gap: '24px' }}
    >
      <div style={{ position: 'relative' }}>
        <span style={iconWrapStyle}>
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
        </span>
        <input
          style={inputStyle}
          autoComplete="username"
          onChange={(e) => setUsername(e.target.value)}
          placeholder="Email ID"
          required
          type="text"
          value={username}
        />
      </div>

      <div style={{ position: 'relative' }}>
        <span style={iconWrapStyle}>
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
        </span>
        <input
          style={inputStyle}
          autoComplete="current-password"
          onChange={(e) => setPassword(e.target.value)}
          placeholder="Password"
          required
          type="password"
          value={password}
        />
      </div>

      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <label
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            color: 'rgba(9,21,76,0.65)',
            fontSize: '0.82rem',
            cursor: 'pointer',
          }}
        >
          <input type="checkbox" style={{ accentColor: '#ffae1b' }} />
          Remember me
        </label>
        <button
          type="button"
          style={{
            background: 'none',
            border: 'none',
            color: '#ffae1b',
            fontSize: '0.82rem',
            cursor: 'pointer',
            fontStyle: 'italic',
            padding: 0,
          }}
        >
          Forgot Password?
        </button>
      </div>

      {error && (
        <p
          style={{
            margin: 0,
            color: '#c9505c',
            fontSize: '0.85rem',
            textAlign: 'center',
            background: 'rgba(255,174,27,0.12)',
            padding: '10px 14px',
            borderRadius: '8px',
            border: '1px solid rgba(255,174,27,0.25)',
          }}
        >
          {error}
        </p>
      )}

      <button
        type="submit"
        disabled={isSubmitting}
        style={{
          width: '100%',
          height: '48px',
          background: '#09154c',
          border: '1px solid #ffae1b',
          borderRadius: '6px',
          color: '#fffefe',
          fontSize: '0.82rem',
          fontWeight: 600,
          letterSpacing: '0.22em',
          textTransform: 'uppercase',
          cursor: isSubmitting ? 'progress' : 'pointer',
          opacity: isSubmitting ? 0.7 : 1,
          transition: 'background 0.2s, border-color 0.2s',
        }}
        onMouseEnter={(e) => {
          e.currentTarget.style.background = '#5ab43f';
          e.currentTarget.style.borderColor = '#5ab43f';
        }}
        onMouseLeave={(e) => {
          e.currentTarget.style.background = '#09154c';
          e.currentTarget.style.borderColor = '#ffae1b';
        }}
      >
        {isSubmitting ? 'Signing in...' : 'Login'}
      </button>

      <p
        style={{
          margin: 0,
          textAlign: 'center',
          fontSize: '0.75rem',
          color: 'rgba(9,21,76,0.42)',
          letterSpacing: '0.04em',
        }}
      >
        Rwanda Revenue Authority · Secure Access
      </p>
    </form>
  );
}
