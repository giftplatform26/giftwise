// Single source for occasion and status presentation. Keep in sync with the backend
// OccasionType and EventStatus enums (see tests/event-labels.test.mjs).
import {localizeText} from './localization.mjs';

export const OCCASION_DETAILS = {
  WEDDING: {icon: '💍', label: 'Wedding', theme: 'wedding'},
  BIRTHDAY: {icon: '🎂', label: 'Birthday', theme: 'birthday'},
  BABY_SHOWER: {icon: '🧸', label: 'Baby shower', theme: 'baby'},
  HOUSEWARMING: {icon: '🏡', label: 'Housewarming', theme: 'home'},
  ANNIVERSARY: {icon: '🥂', label: 'Anniversary', theme: 'anniversary'},
  FAREWELL: {icon: '💐', label: 'Farewell', theme: 'other'},
  RETIREMENT: {icon: '🌅', label: 'Retirement', theme: 'other'},
  REUNION: {icon: '🤝', label: 'Reunion', theme: 'other'},
  OTHER: {icon: '✨', label: 'Other celebration', theme: 'other'},
};

export const STATUS_DETAILS = {
  DRAFT: {label: 'Draft', hint: 'Only you can see this celebration.'},
  PUBLISHED: {label: 'Published', hint: 'Guests with the invitation link can view it.'},
  COMPLETED: {label: 'Completed', hint: 'This celebration has finished.'},
  CANCELLED: {label: 'Cancelled', hint: 'This celebration was cancelled.'},
};

export function occasionIcon(type) { return (OCCASION_DETAILS[type] || OCCASION_DETAILS.OTHER).icon; }
export function occasionTheme(type) { return (OCCASION_DETAILS[type] || OCCASION_DETAILS.OTHER).theme; }
export function occasionLabel(type) { return localizeText((OCCASION_DETAILS[type] || OCCASION_DETAILS.OTHER).label); }
export function statusLabel(status) {
  const key = typeof status === 'string' ? status.toUpperCase() : '';
  return localizeText(STATUS_DETAILS[key]?.label || 'Unknown');
}
export function statusHint(status) {
  const key = typeof status === 'string' ? status.toUpperCase() : '';
  return STATUS_DETAILS[key] ? localizeText(STATUS_DETAILS[key].hint) : '';
}
/** Which lifecycle actions the backend allows from a status (mirrors Event.transition). */
export function lifecycleActions(status) {
  return {
    canEdit: status === 'DRAFT' || status === 'PUBLISHED',
    canComplete: status === 'PUBLISHED',
    canCancel: status === 'DRAFT' || status === 'PUBLISHED',
  };
}
