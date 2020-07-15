<template>
  <div class="row preview" :style="style">
    <div class="col-sm-12 ">
      <functionTop></functionTop>
      <uml :height="umlH"></uml>
    </div>
  </div>
</template>

<script lang="js">
  /* @flow */

  import FunctionTop from './FunctionTop'
  import Uml from './Uml'

  export default {
    name: 'Preview',
    components: {
      FunctionTop,
      Uml
    },
    props: {
      umlH: {
        type: String,
        default: '0px'
      },
    },
    computed: {
      style(): any {
        return {
          height: this.height,
          '--color': this.$store.getters['plantumlEditor/themeColor']
        }
      }
    }
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  .preview {
    --color: #000;

    margin-bottom: -20px;
    padding-bottom: 20px;
    overflow-y: auto;
    border-top: 1px solid white;
    padding-top: 12px;
  }
</style>
