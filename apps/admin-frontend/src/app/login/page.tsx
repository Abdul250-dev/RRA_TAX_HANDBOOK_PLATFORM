import Image from 'next/image';

import { LoginForm } from '../../components/auth/LoginForm';

export default function LoginPage() {
  return (
    <main className="grid min-h-screen place-items-center bg-[#5196CF] px-5 py-8 sm:px-6">
      <div className="flex w-full max-w-[380px] flex-col items-center gap-7 rounded-3xl border border-[rgba(81,150,207,0.18)] bg-[var(--rra-white)] px-8 py-10 shadow-[0_18px_40px_rgba(9,21,76,0.08)] sm:px-10">
        <div className="flex flex-col items-center gap-4">
          <div className="flex h-[76px] w-[76px] items-center justify-center rounded-full border-2 border-[var(--rra-orange)] bg-[var(--rra-white)] shadow-[0_10px_24px_rgba(9,21,76,0.08)]">
            <Image
              alt="RRA Logo"
              className="rounded-full object-contain"
              height={52}
              priority
              src="/assets/bg_rra_logo.png"
              style={{ height: "auto", width: "auto" }}
              width={52}
            />
          </div>
          <h1 className="m-0 text-center text-[0.82rem] font-semibold uppercase tracking-[0.28em] text-[var(--rra-blue)]">
            User Login
          </h1>
        </div>

        <LoginForm />
      </div>
    </main>
  );
}
