import React, { useState, useCallback, useEffect } from 'react'
import ReactDOM from 'react-dom/client'
import { Deck } from '@enriquefft/prez'
import { createSlides } from './slides'
import { useTranslations } from './i18n'
import './styles.css'

function FullscreenButton() {
  const [isFs, setIsFs] = useState(false)
  const [supported] = useState(() =>
    typeof document.documentElement.requestFullscreen === 'function'
  )

  useEffect(() => {
    const handler = () => setIsFs(!!document.fullscreenElement)
    document.addEventListener('fullscreenchange', handler)
    return () => document.removeEventListener('fullscreenchange', handler)
  }, [])

  const toggle = useCallback(() => {
    if (document.fullscreenElement) {
      document.exitFullscreen()
    } else {
      document.documentElement.requestFullscreen()
    }
  }, [])

  if (!supported) return null

  return (
    <button
      onClick={toggle}
      aria-label={isFs ? 'Exit fullscreen' : 'Enter fullscreen'}
      style={{
        position: 'fixed',
        bottom: 16,
        right: 16,
        zIndex: 9999,
        width: 40,
        height: 40,
        borderRadius: 8,
        border: 'none',
        background: 'rgba(255,255,255,0.15)',
        backdropFilter: 'blur(8px)',
        color: 'white',
        fontSize: 18,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        cursor: 'pointer',
        opacity: 0.6,
        transition: 'opacity 0.2s',
      }}
      onPointerEnter={e => (e.currentTarget.style.opacity = '1')}
      onPointerLeave={e => (e.currentTarget.style.opacity = '0.6')}
    >
      {isFs ? '⊡' : '⛶'}
    </button>
  )
}

const t = useTranslations()
const slides = createSlides(t)

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <Deck>
      {slides}
    </Deck>
    <FullscreenButton />
  </React.StrictMode>,
)
