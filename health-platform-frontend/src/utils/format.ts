export const formatDate = (value?: string | number | Date, fallback = '-') => {
  if (!value) return fallback;
  const date = value instanceof Date ? value : new Date(value);
  if (Number.isNaN(date.getTime())) return fallback;
  return date.toISOString().slice(0, 19).replace('T', ' ');
};

export const formatTime = (value?: string) => value ?? '--:--';
