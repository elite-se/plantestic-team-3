<template>
  <div id="help" class="modal fade">
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <button type="button" class="close" data-dismiss="modal">
            <span>&times;</span>
          </button>
          <div class="h3 modal-title">Plantestic PlantUML Editor</div>
        </div>
        <div class="modal-body">
          <p>
            Graphical tool for the Plantestic code generation tool, based on the PlantUML language.
          </p>
          <div class="page-header">
            <span class="h4">Based on</span>
          </div>
          <dl>
            <dt>PlantUML Web-Editor by kkeisuke</dt>
            <dd>
              <ul>
                <li>
                  <a href="https://github.com/kkeisuke/plantuml-editor" target="_blank">https://github.com/kkeisuke/plantuml-editor</a>
                </li>
                <li>
                  <a href="https://github.com/kkeisuke/plantuml-server" target="_blank">https://github.com/kkeisuke/plantuml-server</a>
                </li>
              </ul>
            </dd>
            <dt>PlantUML</dt>
            <dd>
              <ul>
                <li>
                  <a href="http://ogom.github.io/draw_uml/plantuml" target="_blank">English</a>
                </li>
              </ul>
            </dd>
          </dl>

          <div class="page-header">
            <span class="h4">Other</span>
          </div>
          <p>
            Pen graphic by
            <a href="http://www.flaticon.com/authors/freepik" target="_blank">freepik</a>
            from
            <a href="http://www.flaticon.com/" target="_blank">Flaticon</a> is licensed under
            <a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC BY
              3.0</a>. Check out the new logo that I created on
            <a href="http://logomakr.com" title="Logo Maker" target="_blank">LogoMaker.com</a>
            https://logomakr.com/7Gn1Ck
          </p>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-default" data-dismiss="modal">
            Close
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="js">
  /* @flow */

  export default {
    name: 'HelpModal',
    computed: {
      official(): string {
        return this.$store.state.plantumlEditor.official
      }
    }
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped></style>
