// src/utils/dateFormatter.js
const robustDateParse = (dateStringOrObject) => {
  if (!dateStringOrObject) return null;
  if (dateStringOrObject instanceof Date) return dateStringOrObject;
  let parsableDateString = String(dateStringOrObject);
  if (parsableDateString.length === 19 && parsableDateString.charAt(10) === ' ') {
    parsableDateString = parsableDateString.replace(' ', 'T');
  }
  const date = new Date(parsableDateString);
  if (isNaN(date.getTime())) {
    console.warn("Formatter: Failed to parse date:", dateStringOrObject, "Attempted with:", parsableDateString);
    return null;
  }
  return date;
};

export const formatDate = (dateInput) => {
  const date = robustDateParse(dateInput);
  if (!date) return 'N/A'; // Or 'Invalid Date'
  return date.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' });
};

export const formatDateTime = (dateInput) => {
  const date = robustDateParse(dateInput);
  if (!date) return 'N/A'; // Or 'Invalid Date'
  return date.toLocaleString(undefined, { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
};