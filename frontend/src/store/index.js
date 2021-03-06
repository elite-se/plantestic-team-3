/* @flow */

import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

import plantumlEditor from './modules/PlantumlEditor'
import layout from './modules/Layout'
import histories from './modules/Histories'
import umlTemplate from './modules/UmlTemplate'
import tomlTemplate from './modules/TomlTemplate'
import cheatSheet from './modules/CheatSheet'
import gistApi from './modules/GistApi'
import notifications from './modules/Notifications'

export default new Vuex.Store({
  strict: process.env.NODE_ENV !== 'production',
  modules: {
    plantumlEditor,
    layout,
    histories,
    umlTemplate,
    tomlTemplate,
    cheatSheet,
    gistApi,
    notifications
  },
})
