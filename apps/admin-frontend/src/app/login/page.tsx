import Image from 'next/image';
import { LoginForm } from '../../components/auth/LoginForm';

export default function LoginPage() {
  return (
    <main
      style={{
        minHeight: '100vh',
        display: 'grid',
        placeItems: 'center',
        background: '#5196CF',
      }}
    >
      <div
        style={{
          width: 'min(380px, calc(100% - 40px))',
          background: '#fffefe',
          border: '1px solid rgba(81,150,207,0.18)',
          borderRadius: '24px',
          padding: '40px 32px',
          boxShadow: '0 18px 40px rgba(9,21,76,0.08)',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          gap: '28px',
        }}
      >
        <div
          style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '16px' }}
        >
          <div
            style={{
              width: '76px',
              height: '76px',
              borderRadius: '50%',
              background: '#fffefe',
              border: '2px solid #ffae1b',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              boxShadow: '0 10px 24px rgba(9,21,76,0.08)',
            }}
          >
            <Image
              alt="RRA Logo"
              src="/assets/bg_rra_logo.png"
              width={52}
              height={52}
              priority
              style={{ objectFit: 'contain', borderRadius: '50%' }}
            />
          </div>
          <h1
            style={{
              margin: 0,
              color: '#09154c',
              fontSize: '0.82rem',
              fontWeight: 600,
              letterSpacing: '0.28em',
              textTransform: 'uppercase',
            }}
          >
            Admin Login
          </h1>
        </div>

        <LoginForm />
      </div>
    </main>
  );
}
