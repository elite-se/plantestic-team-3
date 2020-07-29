<template>
  <div :style="{ height: height }">
    <codemirror :id="id" :value="text" :options="options" @ready="onReady" @input="onChange"></codemirror>
  </div>
</template>

<script lang="js">
  /* @flow */

  import CodeMirror from 'codemirror/lib/codemirror.js'
  import {codemirror} from 'vue-codemirror'
  import 'codemirror/keymap/sublime.js'
  import 'codemirror/keymap/vim.js'
  import 'codemirror/keymap/emacs.js'
  import 'codemirror/addon/selection/active-line.js'
  import 'codemirror/addon/hint/show-hint.js'
  import 'codemirror/addon/comment/comment.js'
  import 'codemirror/mode/toml/toml.js'

  import 'codemirror/lib/codemirror.css'

  // theme
  import 'codemirror/theme/base16-dark.css'
  import 'codemirror/theme/hopscotch.css'
  import 'codemirror/theme/material.css'
  import 'codemirror/theme/mbo.css'
  import 'codemirror/theme/paraiso-dark.css'
  import 'codemirror/theme/railscasts.css'
  import 'codemirror/theme/seti.css'
  import 'codemirror/theme/shadowfox.css'
  import 'codemirror/theme/solarized.css'
  import 'codemirror/theme/tomorrow-night-eighties.css'

  // addon
  import 'codemirror/addon/hint/show-hint.css'

  export default {
    name: 'TomlEditor',
    components: {
      codemirror
    },
    props: {
      id: String,
      height: {
        type: String,
        default: '100%'
      }
    },
    data(): any {
      return {
        codemirror: null,
      }
    },
    computed: {
      text(): string {
        return this.$store.state.plantumlEditor.configText
      },
      options(): any {
        return this.$store.state.plantumlEditor.codemirrorOptionsToml
      }
    },
    methods: {
      onReady(codemirror: any) {
        this.codemirror = codemirror
        setTimeout(() => {
          this.codemirror.setSize('100%', 'calc(100%)')
        })
      },
      onChange(text: string) {
        this.$store.dispatch('plantumlEditor/syncConfigText', text)
      },
      refreshEditor() {
        //foce rerendering of editor. Needed when this editor is hidden by default and only shown interactively by bootstrap tabs
        if (this.codemirror) {
          this.codemirror.refresh();
        }
      }
    }
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  .vue-codemirror {
    height: 100%;
  }
</style>
