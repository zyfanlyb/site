import DefaultTheme from 'vitepress/theme'
import BlogList from '../components/BlogList.vue'
import AgentAuthGate from '../components/AgentAuthGate.vue'
import './custom.css'
import AuthGateLayout from './AuthGateLayout.vue'

export default {
  extends: DefaultTheme,
  Layout: AuthGateLayout,
  enhanceApp({ app }) {
    app.component('BlogList', BlogList)
    app.component('AgentAuthGate', AgentAuthGate)
  }
}

