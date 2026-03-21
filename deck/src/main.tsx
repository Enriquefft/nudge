import React from 'react'
import ReactDOM from 'react-dom/client'
import { Deck } from '@enriquefft/prez'
import { createSlides } from './slides'
import { useTranslations } from './i18n'
import './styles.css'

const t = useTranslations()
const slides = createSlides(t)

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <Deck>
      {slides}
    </Deck>
  </React.StrictMode>,
)
