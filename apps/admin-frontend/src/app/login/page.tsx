import Image from 'next/image';
import { LoginForm } from '../../components/auth/LoginForm';

export default function LoginPage() {
  return (
    <main
      style={{
        minHeight: '100vh',
        display: 'grid',
        placeItems: 'center',
        background: 'linear-gradient(135deg, #09154c 0%, #09154c 40%, #5196cf 100%)',
      }}
    >
      <div
        style={{
          width: 'min(420px, calc(100% - 48px))',
          background: 'rgba(255,254,254,0.08)',
          backdropFilter: 'blur(24px)',
          WebkitBackdropFilter: 'blur(24px)',
          border: '1px solid rgba(255,254,254,0.15)',
          borderRadius: '24px',
          padding: '48px 40px',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          gap: '32px',
        }}
      >
        {/* Logo circle */}
        <div
          style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '16px' }}
        >
          <div
            style={{
              width: '76px',
              height: '76px',
              borderRadius: '50%',
              background: '#09154c',
              border: '2px solid #ffae1b',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
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
              color: '#fffefe',
              fontSize: '0.82rem',
              fontWeight: 400,
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
