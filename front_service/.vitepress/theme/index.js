import DefaultTheme from 'vitepress/theme'
import BlogList from '../components/BlogList.vue'
import './custom.css'

export default {
  extends: DefaultTheme,
  enhanceApp({ app }) {
    app.component('BlogList', BlogList)
  }
}

