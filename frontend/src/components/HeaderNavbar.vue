<template>
  <div class="navbar navbar-inverse navbar-static-top">
    <div class="container-fluid">
      <ul class="nav navbar-nav navbar-left">
        <li>
          <a href="#" @click.prevent="changeHistoryColSize">
            <span class="glyphicon glyphicon-menu-hamburger" :class="{ 'fa-rotate-90': isCloseHistory }"></span>
          </a>
        </li>
      </ul>
      <div class="navbar-header">
        <a class="navbar-brand" href="#" @click.prevent>Plantestic Editor <span class="h6">beta</span></a>
      </div>
      <umlTemplate></umlTemplate>
      <ul class="nav navbar-nav">
        <li>
          <a href="#" data-toggle="modal" data-target="#options"> <span class="glyphicon glyphicon-cog"></span> Options </a>
        </li>
      </ul>
      <ul class="nav navbar-nav">
        <li>
          <a href="#" data-toggle="modal" data-target="#help"> <span class="glyphicon glyphicon-question-sign"></span> Help </a>
        </li>
      </ul>
    </div>
  </div>
</template>

<script lang="js">
/* @flow */

import UmlTemplate from './UmlTemplate'

export default {
  name: 'HeaderNavbar',
  components: {
    UmlTemplate
  },
  computed: {
    isCloseHistory(): string {
      return this.$store.getters['layout/isCloseHistory']
    }
  },
  methods: {
    changeHistoryColSize() {
      if (this.$store.state.layout.colSize.history) {
        this.$store.dispatch('layout/setEditColSize')
      } else {
        this.$store.dispatch('layout/resetColSize')
      }
    }
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped></style>
