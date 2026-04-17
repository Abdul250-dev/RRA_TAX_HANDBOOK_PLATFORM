'use client';

import React, { ReactNode, useState } from 'react';

interface UtilBarProps {
  onSearch: () => void;
  onLanguageChange?: (lang: string) => void;
}

export const UtilBar: React.FC<UtilBarProps> = ({ onSearch, onLanguageChange }) => {
  return (
    <div
      style={{
        background: '#1a3a6b',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'flex-end',
        padding: '0 16px',
        height: '38px',
        gap: '12px',
        flexWrap: 'wrap',
      }}
    >
      <div style={{ position: 'relative' }}>
        <button
          style={{
            color: '#d0dff5',
            fontSize: '12px',
            background: 'none',
            border: 'none',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: '5px',
          }}
        >
          🇬🇧 English
          <svg width="9" height="9" viewBox="0 0 10 10" fill="currentColor">
            <path d="M1 3l4 4 4-4" />
          </svg>
        </button>
      </div>
      <button
        onClick={onSearch}
        style={{
          color: '#d0dff5',
          fontSize: '12px',
          background: 'none',
          border: 'none',
          cursor: 'pointer',
        }}
      >
        Search
      </button>
    </div>
  );
};

interface HeaderProps {
  onLogoClick: () => void;
  onBackClick?: () => void;
  showMobileMenu?: boolean;
}

export const Header: React.FC<HeaderProps> = ({ onLogoClick, onBackClick, showMobileMenu }) => {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  return (
    <header
      style={{
        background: '#fff',
        borderBottom: '3px solid #1a3a6b',
        padding: '0 16px',
        display: 'flex',
        alignItems: 'center',
        height: 'auto',
        minHeight: '60px',
        gap: '12px',
        flexWrap: 'wrap',
        justifyContent: 'space-between',
      }}
    >
      <img
        onClick={onLogoClick}
        src="/RRA_Logo_home.png"
        alt="RRA Logo"
        style={{
          height: '45px',
          cursor: 'pointer',
          display: 'flex',
          alignItems: 'center',
        }}
      />

      <div
        style={{
          width: '1px',
          height: '36px',
          background: '#d0d9e8',
          margin: '0 8px',
          display: window.innerWidth > 768 ? 'block' : 'none',
        }}
      />

      <div
        style={{
          fontSize: '13px',
          fontWeight: 500,
          color: '#1a3a6b',
          display: window.innerWidth > 768 ? 'block' : 'none',
          flexGrow: 1,
        }}
      >
        Tax Handbook-Edition 2025
      </div>

      <a
        href="https://www.rra.gov.rw"
        target="_blank"
        rel="noopener noreferrer"
        style={{
          display: window.innerWidth > 640 ? 'flex' : 'none',
          alignItems: 'center',
          gap: '6px',
          color: '#1a5fa8',
          fontSize: '12px',
          textDecoration: 'none',
          whiteSpace: 'nowrap',
        }}
      >
        Visit RRA ➜
      </a>
    </header>
  );
};

interface BreadcrumbProps {
  items: Array<{ label: string; onClick: () => void }>;
}

export const Breadcrumb: React.FC<BreadcrumbProps> = ({ items }) => {
  return (
    <div
      style={{
        background: '#f5f8fd',
        borderBottom: '1px solid #dce6f5',
        padding: '8px 16px',
        display: 'flex',
        alignItems: 'center',
        gap: '6px',
        fontSize: '11px',
        color: '#6a7a9a',
        flexWrap: 'wrap',
        overflowX: 'auto',
      }}
    >
      {items.map((item, idx) => (
        <React.Fragment key={idx}>
          {idx > 0 && <span style={{ color: '#9aabbc' }}>›</span>}
          <button
            onClick={item.onClick}
            style={{
              color: '#1a5fa8',
              textDecoration: 'none',
              border: 'none',
              background: 'none',
              cursor: 'pointer',
              fontSize: 'inherit',
            }}
          >
            {item.label}
          </button>
        </React.Fragment>
      ))}
    </div>
  );
};

interface FooterProps {
  children?: ReactNode;
}

export const Footer: React.FC<FooterProps> = ({ children }) => (
  <footer
    style={{
      background: '#1a3a6b',
      padding: '12px 16px',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      flexWrap: 'wrap',
      gap: '8px',
      fontSize: '11px',
    }}
  >
    <span style={{ color: '#a0b4d0' }}>
      Copyright © 2025 All Rights Reserved by Rwanda Revenue Authority
    </span>
    {children}
  </footer>
);

interface SearchOverlayProps {
  isOpen: boolean;
  onClose: () => void;
  onSearch?: (query: string) => void;
}

export const SearchOverlay: React.FC<SearchOverlayProps> = ({ isOpen, onClose, onSearch }) => {
  const [query, setQuery] = React.useState('');

  return (
    <div
      style={{
        display: isOpen ? 'flex' : 'none',
        position: 'fixed',
        inset: 0,
        background: 'rgba(20, 40, 80, 0.7)',
        zIndex: 500,
        alignItems: 'flex-start',
        justifyContent: 'center',
        paddingTop: window.innerWidth <= 640 ? '20px' : '80px',
      }}
      onClick={onClose}
    >
      <div
        style={{
          background: '#fff',
          borderRadius: '8px',
          width: 'min(560px, 90%)',
          padding: '16px',
          boxShadow: '0 20px 60px rgba(0, 0, 0, 0.3)',
        }}
        onClick={(e) => e.stopPropagation()}
      >
        <input
          type="text"
          placeholder="Search the tax handbook..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === 'Enter' && onSearch) {
              onSearch(query);
            }
          }}
          style={{
            width: '100%',
            padding: '10px',
            fontSize: '14px',
            border: '1px solid #dce6f5',
            borderRadius: '4px',
          }}
          autoFocus
        />
      </div>
    </div>
  );
};
