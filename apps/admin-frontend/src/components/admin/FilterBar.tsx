export interface FilterOption {
  label: string;
  active?: boolean;
}

export function FilterBar({ options }: { options: FilterOption[] }) {
  return (
    <div className="filter-bar" aria-label="Filters">
      {options.map((option) => (
        <button
          key={option.label}
          className={`filter-chip${option.active ? " filter-chip-active" : ""}`}
          type="button"
        >
          {option.label}
        </button>
      ))}
    </div>
  );
}
