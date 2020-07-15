<template>
  <div id="notification" class="modal fade">
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <button type="button" class="close" data-dismiss="modal">
            <span>&times;</span>
          </button>
          <div class="h3 modal-title">{{heading}}</div>
        </div>
        <div class="modal-body">
          <p>
            {{message}}
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
    name: 'NotificationModal',
    computed: {
      heading(): string {
        return this.$store.state.notifications.heading
      },
      message(): string {
        return this.$store.state.notifications.message
      }
    }
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped></style>
