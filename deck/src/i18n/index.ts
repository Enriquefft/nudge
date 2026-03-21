import en, { type Translations } from './en'
import es from './es'

const translations: Record<string, Translations> = { en, es }

export type { Translations }

export function getLang(): string {
  const params = new URLSearchParams(window.location.search)
  return params.get('lang') ?? 'en'
}

export function useTranslations(lang?: string): Translations {
  const locale = lang ?? getLang()
  return translations[locale] ?? translations.en
}
