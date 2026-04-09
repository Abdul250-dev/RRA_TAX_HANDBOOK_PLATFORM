export interface DashboardStatItem {
  label: string;
  value: string;
  helper: string;
  icon: string;
  tone?: "blue" | "orange" | "green";
}

export function DashboardStats({ items }: { items: DashboardStatItem[] }) {
  return (
    <section className="stats-grid">
      {items.map((item) => {
        const tone = item.tone ?? "blue";

        return (
          <article key={item.label} className="stat-card">
            <div className="stat-card-header">
              <div className={`stat-icon stat-icon-${tone}`} aria-hidden="true">
                {item.icon}
              </div>
            </div>

            <div>
              <div className="stat-label">{item.label}</div>
              <div className="stat-value">{item.value}</div>
            </div>

            <div className="stat-helper">{item.helper}</div>
          </article>
        );
      })}
    </section>
  );
}
