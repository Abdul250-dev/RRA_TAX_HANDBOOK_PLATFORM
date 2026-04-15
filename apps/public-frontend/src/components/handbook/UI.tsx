'use client';

import React, { ReactNode, useState } from 'react';

interface TableProps {
  headers: string[];
  rows: (string | ReactNode)[][];
}

export const Table: React.FC<TableProps> = ({ headers, rows }) => (
  <div style={{ overflowX: 'auto', margin: '14px 0 20px', borderRadius: '4px' }}>
    <table
      style={{
        width: '100%',
        borderCollapse: 'collapse',
        fontSize: 'clamp(11px, 2.5vw, 13.5px)',
      }}
    >
      <thead>
        <tr style={{ background: '#1a3a6b' }}>
          {headers.map((header) => (
            <th
              key={header}
              style={{
                background: '#1a3a6b',
                color: '#fff',
                padding: '8px 8px',
                textAlign: 'left',
                fontWeight: 500,
              }}
            >
              {header}
            </th>
          ))}
        </tr>
      </thead>
      <tbody>
        {rows.map((row, idx) => (
          <tr
            key={idx}
            style={{
              background: idx % 2 === 1 ? '#f5f8fd' : '#fff',
            }}
          >
            {row.map((cell, cellIdx) => (
              <td
                key={`${idx}-${cellIdx}`}
                style={{
                  padding: '8px',
                  borderBottom: '1px solid #dce6f5',
                }}
              >
                {cell}
              </td>
            ))}
          </tr>
        ))}
      </tbody>
    </table>
  </div>
);

interface AccordionProps {
  items: Array<{
    title: string;
    content: ReactNode;
  }>;
}

export const Accordion: React.FC<AccordionProps> = ({ items }) => {
  const [openIdx, setOpenIdx] = useState<number | null>(null);

  return (
    <div>
      {items.map((item, idx) => (
        <div
          key={idx}
          style={{
            border: '1px solid #d0d9e8',
            borderRadius: '6px',
            margin: '8px 0',
          }}
        >
          <button
            onClick={() => setOpenIdx(openIdx === idx ? null : idx)}
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              padding: '13px 18px',
              cursor: 'pointer',
              fontSize: '14px',
              fontWeight: 500,
              color: '#1a3a6b',
              background: '#fff',
              border: 'none',
              width: '100%',
              borderRadius: '6px',
              userSelect: 'none',
            }}
            onMouseEnter={(e) =>
              (e.currentTarget.style.background = '#f5f8fd')
            }
            onMouseLeave={(e) =>
              (e.currentTarget.style.background = '#fff')
            }
          >
            {item.title}
            <svg
              width="12"
              height="12"
              viewBox="0 0 12 12"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              style={{
                transform: openIdx === idx ? 'rotate(180deg)' : 'rotate(0deg)',
                transition: 'transform 0.2s',
              }}
            >
              <polyline points="2 4 6 8 10 4" />
            </svg>
          </button>

          {openIdx === idx && (
            <div
              style={{
                display: 'block',
                padding: '16px 20px',
                borderTop: '1px solid #d0d9e8',
                fontSize: '13.5px',
                lineHeight: '1.7',
                color: '#333',
              }}
            >
              {item.content}
            </div>
          )}
        </div>
      ))}
    </div>
  );
};

interface TimelineProps {
  items: Array<{
    year: string;
    text: ReactNode;
  }>;
}

export const Timeline: React.FC<TimelineProps> = ({ items }) => (
  <div
    style={{
      paddingLeft: '20px',
      borderLeft: '2px solid #d0d9e8',
      margin: '16px 0',
    }}
  >
    {items.map((item, idx) => (
      <div
        key={idx}
        style={{
          position: 'relative',
          padding: '0 0 20px 20px',
        }}
      >
        <div
          style={{
            position: 'absolute',
            left: '-8px',
            top: '4px',
            width: '14px',
            height: '14px',
            borderRadius: '50%',
            background: '#1a5fa8',
            border: '2px solid #fff',
            boxShadow: '0 0 0 2px #1a5fa8',
          }}
        />
        <div style={{ fontSize: '12px', fontWeight: 600, color: '#1a5fa8', marginBottom: '4px' }}>
          {item.year}
        </div>
        <div style={{ fontSize: '13.5px', color: '#333', lineHeight: '1.55' }}>
          {item.text}
        </div>
      </div>
    ))}
  </div>
);

interface BackButtonProps {
  label?: string;
  onClick: () => void;
}

export const BackButton: React.FC<BackButtonProps> = ({
  label = 'Back',
  onClick,
}) => (
  <button
    onClick={onClick}
    style={{
      display: 'inline-flex',
      alignItems: 'center',
      gap: '6px',
      color: '#1a5fa8',
      fontSize: '13px',
      cursor: 'pointer',
      border: 'none',
      background: 'none',
      marginTop: '32px',
      padding: 0,
    }}
  >
    ← {label}
  </button>
);

interface BenefitCardProps {
  icon: string;
  text: string;
}

export const BenefitGrid: React.FC<{ items: BenefitCardProps[] }> = ({ items }) => (
  <div
    style={{
      display: 'grid',
      gridTemplateColumns: 'repeat(auto-fill, minmax(150px, 1fr))',
      gap: '10px',
      margin: '14px 0',
    }}
  >
    {items.map((item, idx) => (
      <div
        key={idx}
        style={{
          background: '#f5f8fd',
          border: '1px solid #dce6f5',
          borderRadius: '6px',
          padding: '14px',
          textAlign: 'center',
          fontSize: '13px',
          color: '#1a3a6b',
        }}
      >
        <div style={{ fontSize: '22px', marginBottom: '6px' }}>{item.icon}</div>
        {item.text}
      </div>
    ))}
  </div>
);
