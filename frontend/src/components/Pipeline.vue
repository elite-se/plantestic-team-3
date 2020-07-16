<template>
  <div class="row pipeline" :style="style">
    <div class="col-sm-12">
      <h2 class="heading">Pipeline</h2>

      <div class="alert alert-default">
        <h4><span class="glyphicon glyphicon-compressed"></span> Preprocess</h4>
        <p>Autogenerate variables from swagger API definitions and extract the tester actor (if
          present)</p>
        <p>The preprocessed puml will be returned quickly and appear in the editor in this window.</p>
        <p></p>
        <form class="form-inline">
          <div class="form-group">
            <label for="tester">Tester</label>
            <input type="text" id="tester" style="width: 70px;" v-model="tester"
                   class="form-control"/>
          </div>
        </form>
        <button type="button" class="btn btn-primary" @click="triggerPreprocessing">
          <span class="glyphicon glyphicon-send"></span> Trigger Preprocessing
        </button>
      </div>


      <div class="alert alert-default">
        <h4><span class="glyphicon glyphicon-random"></span> Generate Tests</h4>
        <p>Attempt to generate Acceleo Testcases from the current puml diagram.</p>
        <p></p>
        <form class="form-inline">
          <div class="form-group">
            <label for="testName">Testfile Name</label>
            <input type="text" id="testName" style="width: 70px;" v-model="testFileName"
                   class="form-control"/>
          </div>
        </form>
        <button type="button" class="btn btn-primary" @click="generateTests">
          <span class="glyphicon glyphicon-send"></span> Generate Tests
        </button>
      </div>
    </div>
  </div>
</template>

<script lang="js">
  /* @flow */

  async function sendToServer(url: string = '', data: any = {}): any {
    const response: any = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(data)
    });
    if (response.ok) {
      return await response.json();
    } else {
      throw await response.text();
    }
  }

  async function preprocess(pumlString: string = '', tester: string = '', callback: any, errorCallback: any): any {
    console.log("Triggered Preprocessing");
    try {
      const preprocessedPuml: any = await sendToServer('preprocess', {pumlString: pumlString, tester: tester});
      callback(preprocessedPuml.processedPuml);
    } catch (error) {
      console.log("Error: ", error);
      errorCallback(error);
    }
  }

  async function triggerPipeline(testFileName: string = '', pumlString: string = '', callback: any, errorCallback: any): any {
    console.log("Triggered Pipeline");
    try {
      const pipelineResult: any = await sendToServer('runPipeline', {name: testFileName, diagram: pumlString});
      callback(pipelineResult);
    } catch (error) {
      console.log("Error: ", error);
      errorCallback(error);
    }
  }

  export default {
    name: 'Pipeline',
    data(): any {
      return {
        tester: '',
        testFileName: 'magicMike',
        preprocessing: false,
        pipelining: false
      }
    },
    props: {
      height: {
        type: String,
        default: '100%'
      }
    },
    computed: {
      style(): any {
        return {
          height: this.height,
          '--color': this.$store.getters['plantumlEditor/themeColor']
        }
      }
    },
    methods: {
      triggerPreprocessing() {
        this.preprocessing = true;
        this.$store.dispatch('histories/save', this.$store.state.plantumlEditor)
        preprocess(this.$store.state.plantumlEditor.text, this.tester, (preprocessedPuml: string) => {
            this.$store.dispatch('plantumlEditor/syncText', preprocessedPuml);
            this.preprocessing = false;
            setTimeout(() => {
              this.$store.dispatch('plantumlEditor/renderUML', this.$store.state.plantumlEditor.text)
            }, 300)
          },
          (error: any) => {
            this.preprocessing = false;
            this.showErrorModal("Preprocessing failed, " + error);
          })
      },
      generateTests() {
        this.pipelining = true;
        triggerPipeline(this.testFileName, this.$store.state.plantumlEditor.text, (pipelineResult: any) => {
          this.pipelining = false;
          if (pipelineResult.success) {
            this.$store.dispatch('notifications/setNotificationHeading', "Testcase successfully generated");
            this.$store.dispatch('notifications/setNotificationMessage', "Testcase can be found at " + pipelineResult.filePath);
            window.$('#notification').modal({})
          } else {
            this.showErrorModal("Testcases could not be generated...")
          }
        })
      },
      showErrorModal(errorMessage: string) {
        this.$store.dispatch('notifications/setNotificationHeading', "ERROR!");
        this.$store.dispatch('notifications/setNotificationMessage', errorMessage);
        window.$('#notification').modal({})
      }
    }
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  .pipeline {
    --color: #000;

    margin-top: -20px;
    padding-top: 20px;
    overflow-y: auto;
    background-color: var(--color);
    border-left: 1px solid white;
  }


  .heading {
    color: white;
    margin: 0;
    margin-bottom: 12px;
  }

  form {
    margin: 8px 0;
  }

  form label {
    margin-right: 4px;
  }

  .glyphicon {
    margin-right: 4px;
  }

  button {
    width: 100%;
    padding-top: 10px;
    padding-bottom: 10px;
    margin-top: 20px;
  }
</style>
