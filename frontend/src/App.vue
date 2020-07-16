<template>
  <div id="app">
    <headerNavbar></headerNavbar>
    <div class="container-fluid">
      <div class="row">
        <div :class="[historyCol ? `col-sm-${historyCol}` : 'col-sm-2']" v-show="Boolean(historyCol)">
          <historyList :height="height"></historyList>
        </div>
        <div :class="[ historyCol ?  'col-sm-8' : 'col-sm-10']">
          <div class="row">
            <div class="col-editor" :class="'col-sm-12'">
              <editor :height="editorH"></editor>
            </div>
          </div>
          <Preview :umlH="umlH"></Preview>
        </div>
        <div class="col-sm-2">
          <Pipeline :height="height"></Pipeline>
        </div>
      </div>
      <helpModal></helpModal>
      <optionsModal></optionsModal>
      <NotificationModal></NotificationModal>
    </div>
  </div>
</template>

<script lang="js">
  /* @flow */

  // Bootstrap
  import 'bootstrap'

  // store
  import store from './store'
  import directive from './directive'

  // components
  import HeaderNavbar from './components/HeaderNavbar'
  import HelpModal from './components/HelpModal'
  import OptionsModal from './components/OptionsModal'
  import NotificationModal from "./components/NotificationModal";
  import HistoryList from './components/HistoryList'
  import Pipeline from "./components/Pipeline"
  import Editor from './components/Editor'
  import Preview from "./components/Preview"

  export default {
    name: 'App',
    store,
    directive,
    components: {
      HeaderNavbar,
      HelpModal,
      OptionsModal,
      NotificationModal,
      HistoryList,
      Editor,
      Pipeline,
      Preview
    },
    data(): any {
      return {
        height: '0px',
        umlH: '0px',
        previewH: '0px',
        editorH: '0px'
      }
    },
    computed: {
      historyCol(): number {
        return this.$store.state.layout.colSize.history
      },
      editorCol(): number {
        return this.$store.state.layout.colSize.editor
      },
      umlCol(): number {
        return this.$store.state.layout.colSize.uml
      }
    },
    created() {
      this.resize()
      this.$store.dispatch('plantumlEditor/getLocalStrage')
      this.$store.dispatch('plantumlEditor/renderUML', this.$store.state.plantumlEditor.text)
      this.$store.dispatch('histories/defineScheme')
    },
    mounted() {
      this.setHeight()
      window.$('[data-toggle="tooltip"]').tooltip()
    },
    methods: {
      setHeight() {
        const headerHeight: number = window.$('.navbar-static-top').height()
        const functionTopHeight: number = window.$('.functionTop').height()
        const height: number = window.innerHeight - headerHeight
        const editorHeight: number = height * 0.5
        const marginTop: number = 20
        const marginBottom: number = 10
        this.height = height + 'px'
        this.editorH = editorHeight + 'px'
        this.umlH = (height - editorHeight) - (marginTop + functionTopHeight + marginBottom) + 'px'
      },
      resize() {
        let timer: any = null
        window.addEventListener('resize', () => {
          if (timer) {
            clearTimeout(timer)
          }
          timer = setTimeout(() => {
            this.setHeight()
          }, this.$store.state.plantumlEditor.FPS)
        })
      }
    }
  }
</script>

<style>
  /* font-awesome */
  @import '../node_modules/font-awesome/css/font-awesome.min.css';
  /* Bootstrap */
  @import '../node_modules/bootstrap/dist/css/bootstrap.min.css';
  /* Spinners */
  @import '../node_modules/z-loading/dist/z-loading.css';

  .col-editor {
    margin-top: -20px;
    padding: 0;
  }

  .alert-default {
    background-color: #f5f5f5;
    border-color: #ddd;
  }
</style>
