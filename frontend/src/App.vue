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
            <div role="navigation" class="col-editor">
              <ul class="nav nav-tabs" id="codeEditorTabs" role="tablist" :style="applyThemeColor">
                <li class="nav-item active">
                  <a class="nav-link active" id="plantuml-tab" data-toggle="tab" href="#plantuml" role="tab"
                     aria-controls="plantuml"
                     aria-selected="true">diagram.puml</a>
                </li>
                <li class="nav-item">
                  <a class="nav-link" id="configTab" data-toggle="tab" href="#config" role="tab" aria-controls="config"
                     aria-selected="false">config.toml</a>
                </li>
              </ul>
            </div>
            <div class="tab-content editor-tabs-container" id="codeEditorTabsContent" :class="'col-sm-12'">
              <div class="tab-pane in active" id="plantuml" role="tabpanel">
                <editor :height="editorH" id="puml"></editor>
              </div>

              <div class="tab-pane" id="config" role="tabpanel">
                <toml-editor :height="editorH" ref="toml"></toml-editor>
              </div>
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
  import TomlEditor from './components/TomlEditor'
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
      TomlEditor,
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
      },
      applyThemeColor(): any {
        return {
          '--color': this.$store.getters['plantumlEditor/themeColor']
        }
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
      //Rerender toml editor once its shown, because bootstrap messes up codemirrors initial layout calculations when inside a hidden tab
      window.$('#configTab').on('shown.bs.tab', () => {
        console.log("Refresh toml editor on shown")
        this.$refs.toml.refreshEditor();
      })
    },
    methods: {
      setHeight() {
        const headerHeight: number = window.$('.navbar-static-top').height()
        const functionTopHeight: number = window.$('.functionTop').height()
        const codeEditorTabsHeight: number = window.$('#codeEditorTabs').height()
        const height: number = window.innerHeight - headerHeight
        const editorHeight: number = height * 0.5
        const marginTop: number = 20
        const marginBottom: number = 10
        this.height = height + 'px'
        this.editorH = editorHeight + 'px'
        this.umlH = (height - editorHeight - codeEditorTabsHeight) - (marginTop + functionTopHeight + marginBottom) + 'px'
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

  #codeEditorTabs {
    --color: #123123; /* dynamically set to the selected theme color via js*/

    background-color: var(--color);
  }

  .nav-tabs {
    border-bottom-color: #337ab7;
  }

  .nav-tabs > li.active > a {
    color: white;
    background-color: rgba(255, 255, 255, 0.2);
    border-color: #337ab7;
  }

  .nav-tabs > li.active > a:focus {
    color: white;
    background-color: rgba(255, 255, 255, 0.4);
    border-color: #337ab7;
  }

  .col-editor {
    margin-top: -20px;
  }

  /* declared here at the root level since it is used in several sub-components! */
  .alert-default {
    background-color: #f5f5f5;
    border-color: #ddd;
  }

  .editor-tabs-container {
    padding: 0;
  }
</style>
