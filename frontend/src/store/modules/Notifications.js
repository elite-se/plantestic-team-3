/* @flow */

const state: any = {
  heading: "heading",
  message: "test",
}

const mutations: any = {
  setNotificationMessage(context: any, message: string) {
    state.message = message;
  },
  setNotificationHeading(context: any, heading: string) {
    state.heading = heading;
  }
}

const getters: any = {
}

const actions: any = {
  setNotificationMessage(context: any, message: string) {
    context.commit("setNotificationMessage", message);
  },
  setNotificationHeading(context: any, heading: string) {
    context.commit("setNotificationHeading", heading)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  getters,
  actions,
}
