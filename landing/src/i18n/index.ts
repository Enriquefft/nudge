import en from './en.json';
import es from './es.json';

type Translations = typeof en;

const translations: Record<string, Translations> = { en, es };

export const defaultLocale = 'en' as const;
export const locales = ['en', 'es'] as const;
export type Locale = (typeof locales)[number];

export function useTranslations(locale: string | undefined): Translations {
	return translations[locale ?? defaultLocale] ?? translations[defaultLocale];
}

export function getAlternateLocale(locale: string | undefined): Locale {
	return (locale ?? defaultLocale) === 'en' ? 'es' : 'en';
}

export function getLocaleLabel(locale: string): string {
	const labels: Record<string, string> = {
		en: 'English',
		es: 'Español',
	};
	return labels[locale] ?? locale;
}
