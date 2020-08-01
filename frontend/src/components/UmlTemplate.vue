<template>
  <ul class="nav navbar-nav">
    <li class="dropdown">
      <a class="dropdown-toggle" data-toggle="dropdown" role="button"> <i class="fa fa-pencil-square"></i> Templates
        <b class="caret"></b> </a>
      <ul class="dropdown-menu">
        <li class="dropdown-header">Sequence Diagrams</li>
        <li><a @click="selectTemplate('minimalHello')">Minimal Hello</a></li>
        <li><a @click="selectTemplate('complexHello')">Complex Hello</a></li>
        <li><a @click="selectTemplate('rerouting')">Rerouting</a></li>
        <li><a @click="selectTemplate('xcall')">XCall</a></li>
        <li><a @click="selectTemplate('async')">Async Request</a></li>
        <li><a @click="selectTemplate('demo')">Demo</a></li>
      </ul>
    </li>
  </ul>
</template>

<script lang="js">
  /* @flow */

  export default {
    name: 'UmlTemplate',
    data(): any {
      return {
        selectMessage: 'Do you want to select a template?'
      }
    },
    methods: {
      selectTemplate(prop: string) {
        this.$store.dispatch('umlTemplate/selectTemplate', prop)
        this.$store.dispatch('tomlTemplate/selectTemplate', prop)
        this.$store.dispatch('plantumlEditor/syncFileName', prop)
      }
    }
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  .dropdown-menu > li > a {
    cursor: pointer;
  }
</style>
