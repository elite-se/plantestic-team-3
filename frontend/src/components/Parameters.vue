<template>
  <form class="form-inline">
    <div class="form-group">
      <label for="umlWidth">size</label>
      <input type="number" id="umlWidth" step="10" max="300" min="10" style="width: 70px;" v-model="umlWidth"
             class="form-control" :disabled="!isSvg"/>
    </div>
    <div class="form-group">
      <label for="umlExtension">img</label>
      <select id="umlExtension" v-model="umlExtension" class="form-control">
        <option v-for="option in umlExtensions" :value="option.value" :key="option.value">
          {{ option.text }}
        </option>
      </select>
    </div>
    <div class="form-group">
      <div class="btn-group">
        <button type="button" class="btn btn-default" @click="renderUML" data-toggle="tooltip" data-placement="bottom"
                title="refresh" data-container="body">
          <span class="glyphicon glyphicon-refresh" :class="{ 'fa-spin': isLoading }"></span> Refresh
        </button>
        <button type="button" class="btn btn-default" @click="save" data-toggle="tooltip" data-placement="bottom"
                title="save" data-container="body">
          <span class="glyphicon glyphicon-plus"></span> Add to history
        </button>
        <a :href="src" class="btn btn-default" @click.prevent="download" data-toggle="tooltip" data-placement="bottom"
           title="download" data-container="body">
          <span class="glyphicon glyphicon-download-alt"></span> Download
        </a>
      </div>
    </div>
  </form>
</template>

<script lang="js">
  /* @flow */
  import printjs from 'print-js'

  export default {
    name: 'Parameters',
    computed: {
      src(): string {
        return this.$store.state.plantumlEditor.src
      },
      isSvg(): string {
        return this.$store.getters['plantumlEditor/isSvg']
      },
      isLoading(): boolean {
        return this.$store.state.plantumlEditor.isLoading
      },
      umlWidth: {
        get(): number {
          return this.$store.state.plantumlEditor.umlWidth
        },
        set(value: number) {
          this.$store.dispatch('plantumlEditor/setUmlWidth', value)
        }
      },
      umlExtensions(): any {
        return this.$store.state.plantumlEditor.umlExtensions
      },
      umlExtension: {
        get(): string {
          return this.$store.state.plantumlEditor.umlExtension
        },
        set(value: string) {
          this.$store.dispatch('plantumlEditor/setUmlExtension', value)
          this.$store.dispatch('plantumlEditor/renderUML', this.$store.state.plantumlEditor.text)
        }
      }
    },
    methods: {
      renderUML() {
        this.$store.dispatch('plantumlEditor/renderUML', this.$store.state.plantumlEditor.text)
      },
      save() {
        this.$store.dispatch('histories/save', this.$store.state.plantumlEditor)
        this.$store.dispatch('layout/resetColSize')
      },
      showGistModal() {
        window.$('#gist').modal('show')
        window.$('[data-toggle="tooltip"]').tooltip('hide')
      },
      download() {
        this.$store.dispatch('plantumlEditor/download')
      },
      print() {
        printjs('umlArea', 'html')
      }
    }
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style lang="scss" scoped>
  .form-inline {
    .form-group {
      margin-right: 10px;

      &:last-child {
        margin-right: 0;
      }

      label {
        margin-right: 3px;
      }
    }
  }
</style>
