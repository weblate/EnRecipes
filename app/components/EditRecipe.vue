<template>
<Page @loaded="onPageLoad" @unloaded="releaseBackEvent">
  <ActionBar :androidElevation="viewIsScrolled ? 4 : 0">
    <GridLayout rows="*" columns="auto, *, auto">
      <MDButton variant="text" class="bx" :text="icon.back" automationText="Back" col="0" @tap="navigateBack" />
      <Label class="title orkm" :text="`${title}` | L" col="1" />
      <MDButton variant="text" v-if="hasChanges && !saving" class="bx" :text="icon.save" col="2" @tap="saveOperation" />
      <MDActivityIndicator col="2" v-if="saving" :busy="saving" />
    </GridLayout>
  </ActionBar>
  <ScrollView width="100%" height="100%" @scroll="onScroll">
    <StackLayout width="100%" padding="0 0 88">
      <AbsoluteLayout>
        <StackLayout width="100%" :height="screenWidth" class="imageHolder" verticalAlignment="center">
          <Image v-if="recipeContent.imageSrc" :src="recipeContent.imageSrc" stretch="aspectFill" width="100%" :height="screenWidth" />
          <Label v-else horizontalAlignment="center" class="bx" fontSize="160" :text="icon.image" />
        </StackLayout>
        <transition :name="recipeContent.imageSrc ? 'null' : 'bounce'">
          <MDFloatingActionButton v-if="showFab" :top="screenWidth - 44" :left="screenWidth - 88" class="bx" src="res://camera" @tap="imageHandler" />
        </transition>
      </AbsoluteLayout>
      <StackLayout margin="0 16">
        <AbsoluteLayout class="inputField">
          <TextField :hint="'My Healthy Recipe' | L" v-model="recipeContent.title" @loaded="setInputTypeText($event, 'words')" />
          <Label top="0" class="fieldLabel" :text="'Title' | L" />
        </AbsoluteLayout>
        <GridLayout columns="*, 8, *">
          <AbsoluteLayout class="inputField" col="0">
            <TextField :text="`${recipeContent.cuisine}` | L" editable="false" @focus="modalOpen === false && showCuisine(true)" @tap="showCuisine(false)" />
            <Label top="0" class="fieldLabel" :text="'Cuisine' | L" />
          </AbsoluteLayout>
          <AbsoluteLayout class="inputField" col="2">
            <TextField ref='category' :text="`${recipeContent.category}` | L" editable="false" @focus="modalOpen === false && showCategories(true)" @tap="showCategories(false)" />
            <Label top="0" class="fieldLabel" :text="'Category' | L" />
          </AbsoluteLayout>
        </GridLayout>
        <AbsoluteLayout class="inputField">
          <TextField autocapitalizationType="words" ref='tags' :hint="`${$options.filters.L('separate with spaces')}`" v-model="tags" @textChange="splitTags" returnKeyType="next" />
          <Label top="0" class="fieldLabel" :text="`${$options.filters.L('Tags')} (${$options.filters.L('separate with spaces')})`" />
        </AbsoluteLayout>
        <GridLayout columns="*, 8, *">
          <AbsoluteLayout class="inputField" col="0">
            <TextField :text="timeRequired('prepTime')" editable="false" @focus="
                  modalOpen === false && setTimeRequired(true, 'prepTime')
                " @tap="setTimeRequired(false, 'prepTime')" />
            <Label top="0" class="fieldLabel" :text="'Preparation time' | L" />
          </AbsoluteLayout>
          <AbsoluteLayout class="inputField" col="2">
            <TextField ref="cookTime" :text="timeRequired('cookTime')" editable="false" @focus="
                  modalOpen === false && setTimeRequired(true, 'cookTime')
                " @tap="setTimeRequired(false, 'cookTime')" />
            <Label top="0" class="fieldLabel" :text="'Cooking time' | L" />
          </AbsoluteLayout>
        </GridLayout>
        <GridLayout columns="*, 8, *">
          <AbsoluteLayout class="inputField" col="0">
            <TextField ref="yieldQuantity" v-model="recipeContent.yield.quantity" hint="1" keyboardType="number" returnKeyType="next" />
            <Label top="0" class="fieldLabel" :text="'Yield quantity' | L" />
          </AbsoluteLayout>
          <AbsoluteLayout class="inputField" col="2">
            <TextField :text="`${recipeContent.yield.unit}` | L" editable="false" @focus="modalOpen === false && showYieldUnits(true)" @tap="showYieldUnits(false)" />
            <Label top="0" class="fieldLabel" :text="'Yield measured in' | L" />
          </AbsoluteLayout>
        </GridLayout>
        <GridLayout columns="*, 8, *">
          <AbsoluteLayout class="inputField" col="0">
            <TextField ref="difficultyLevel" :text="`${recipeContent.difficulty}` | L" editable="false" @focus="modalOpen === false && showDifficultyLevel(true)" @tap="showDifficultyLevel(false)" />
            <Label top="0" class="fieldLabel" :text="'Difficulty level' | L" />
          </AbsoluteLayout>
        </GridLayout>
        <StackLayout class="hr" margin="24 16"></StackLayout>
      </StackLayout>
      <StackLayout margin="0 16">
        <Label :text="'Ingredients' | L" class="sectionTitle" />
        <GridLayout columns="auto,8,auto,8,*,8,auto" v-for="(ingredient, index) in recipeContent.ingredients" :key="index">

          <TextField width="60" col="0" @loaded="!recipeContent.ingredients[index].item && focusField($event)" v-model="recipeContent.ingredients[index].quantity" hint="1.00" keyboardType="number" returnKeyType="next" />

          <TextField width="76" col="2" :text="`${recipeContent.ingredients[index].unit}` | L" editable="false" @focus="modalOpen === false && showUnits($event, true, index)" @tap="showUnits($event, false, index)" />

          <TextField ref="ingredient" @loaded="setInputTypeText($event, 'sentence')" col="4" v-model="recipeContent.ingredients[index].item" :hint="`${$options.filters.L('Item')} ${index + 1}`"
            @returnPress="index+1 == recipeContent.ingredients.length && addIngredient()" />

          <MDButton variant="text" col="6" class="bx closeBtn" :text="icon.close" @tap="removeIngredient(index)" />
        </GridLayout>
        <MDButton variant="text" class="text-btn orkm" :text="`+ ${$options.filters.L('ADD INGREDIENT')}`" @tap="addIngredient()" />
        <StackLayout class="hr" margin="24 16"></StackLayout>
      </StackLayout>
      <StackLayout margin="0 16">
        <Label :text="'Instructions' | L" class="sectionTitle" />
        <GridLayout columns="*,8,auto" v-for="(instruction, index) in recipeContent.instructions" :key="index">
          <TextView @loaded="focusField($event, 'multiLine')" col="0" :hint="`${$options.filters.L('Step')} ${index + 1}`" v-model="recipeContent.instructions[index]" />
          <MDButton variant="text" col="2" class="bx closeBtn" :text="icon.close" @tap="removeInstruction(index)" />
        </GridLayout>
        <MDButton variant="text" class="text-btn orkm" :text="`+ ${$options.filters.L('ADD STEP')}`" @tap="addInstruction" />
        <StackLayout class="hr" margin="24 16"></StackLayout>
      </StackLayout>
      <StackLayout margin="0 16">
        <Label :text="'Notes' | L" class="sectionTitle" />
        <GridLayout columns="*,8,auto" v-for="(note, index) in recipeContent.notes" :key="index">
          <TextView @loaded="focusField($event, 'multiLine')" col="0" :hint="`${$options.filters.L('Note')} ${index + 1}`" v-model="recipeContent.notes[index]" />
          <MDButton variant="text" col="2" class="bx closeBtn" :text="icon.close" @tap="removeNote(index)" />
        </GridLayout>
        <MDButton variant="text" class="text-btn orkm" :text="`+ ${$options.filters.L('ADD NOTE')}`" @tap="addNote" />
        <StackLayout class="hr" margin="24 16"></StackLayout>
      </StackLayout>
      <StackLayout margin="0 16">
        <Label :text="'Combinations' | L" class="sectionTitle" />
        <GridLayout columns="*,8,auto" v-for="(combination, index) in recipeContent.combinations" :key="index">
          <TextField class="combinationToken" col="0" :text="getCombinationTitle(combination)" editable="false" />
          <MDButton variant="text" col="2" class="bx closeBtn" :text="icon.close" @tap="removeCombination(combination)" />
        </GridLayout>
        <MDButton variant="text" class="text-btn orkm" :text="`+ ${$options.filters.L('ADD COMBINATION')}`" @tap="showCombinations" />
      </StackLayout>
    </StackLayout>
  </ScrollView>
</Page>
</template>

<script>
import {
  AndroidApplication,
  Application,
  ApplicationSettings,
  File,
  getFileAccess,
  ImageSource,
  knownFolders,
  path,
  Screen,
  Utils,
  Observable
}
from "@nativescript/core"
import * as Permissions from "@nativescript-community/perms"
import * as Toast from "nativescript-toast"
import * as Filepicker from "nativescript-plugin-filepicker"
import {
  ImageCropper
}
from "nativescript-imagecropper"
import {
  localize
}
from "@nativescript/localize"
import {
  mapState,
  mapActions
}
from "vuex"
import ViewRecipe from "./ViewRecipe.vue"
import ActionDialog from "./modal/ActionDialog.vue"
import ActionDialogWithSearch from "./modal/ActionDialogWithSearch.vue"
import ConfirmDialog from "./modal/ConfirmDialog.vue"
import PromptDialog from "./modal/PromptDialog.vue"
import ListPicker from "./modal/ListPicker.vue"
import * as utils from "~/shared/utils"
export default {
  props: [ "recipeID", "selectedCuisine", "selectedCategory", "selectedTag", "filterFavourites", "filterTrylater", "navigationFromView", ],
  data() {
    return {
      title: "New recipe",
      viewIsScrolled: false,
      recipeContent: {
        imageSrc: null,
        title: undefined,
        cuisine: "Undefined",
        category: "Undefined",
        tags: [],
        prepTime: "00:00",
        cookTime: "00:00",
        yield: {
          quantity: undefined,
          unit: "Serving",
        },
        difficulty: "Easy",
        rating: 0,
        ingredients: [],
        instructions: [],
        combinations: [],
        notes: [],
        isFavorite: false,
        tried: true,
        lastTried: null,
        lastModified: null,
        created: null,
      },
      tempRecipeContent: {},
      tags: undefined,
      blockModal: false,
      modalOpen: false,
      newRecipeID: null,
      showFab: false,
      saving: false,
      cacheImagePath: null,
      unSyncCombinations: [],
      difficultyLevels: [
        "Easy",
        "Moderate",
        "Challenging",
      ],
    }
  },
  computed: {
    ...mapState( [ "icon", "units", "yieldUnits", "recipes", "cuisines", "categories", "currentComponent", ] ),
    screenWidth() {
      return Screen.mainScreen.widthDIPs
    },
    appTheme() {
      return Application.systemAppearance()
    },
    hasChanges() {
      return ( JSON.stringify( this.recipeContent ) !== JSON.stringify( this.tempRecipeContent ) )
    },
  },
  methods: {
    ...mapActions( [ "setCurrentComponentAction", "addRecipeAction", "overwriteRecipeAction", "addListItemAction", "unSyncCombinationsAction", ] ),
    onPageLoad( args ) {
      const page = args.object;
      page.bindingContext = new Observable();
      this.showFab = true
    },
    timeRequired( time ) {
      let t = this.recipeContent[ time ].split( ":" )
      let h = parseInt( t[ 0 ] )
      let m = parseInt( t[ 1 ] )
      let hr = localize( 'hr' )
      let min = localize( 'min' )
      return h ? ( m ? `${h} ${hr} ${m} ${min}` : `${h} ${hr}` ) : `${m} ${min}`
    },
    // HELPERS
    focusField( args, type ) {
      if ( type ) this.setInputTypeText( args, type )
      if ( !args.object.text ) {
        args.object.focus()
        setTimeout( ( e ) => Utils.ad.showSoftInput( args.object.android ), 100 )
      }
    },
    setInputTypeText( args, type ) {
      let field = args.object
      let common = android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
      switch ( type ) {
        case "words":
          field.android.setInputType( android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS | common )
          break
        case "sentence":
          field.android.setInputType( android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | common )
          break
        case "multiLine":
          field.android.setInputType( android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE | android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | common )
          break
        default:
          break
      }
    },
    getRandomID() {
      let res = ""
      let chars = "abcdefghijklmnopqrstuvwxyz0123456789"
      for ( let i = 0; i < 20; i++ ) {
        res += chars.charAt( Math.floor( Math.random() * chars.length ) )
      }
      return res
    },
    setTimeRequired( focus, time ) {
      this.modalOpen = true
      let t = this.recipeContent[ time ].split( ":" )
      let hr = t[ 0 ]
      let min = t[ 1 ]
      this.$showModal( ListPicker, {
        props: {
          title: `${time == "prepTime" ? "Preparation" : "Cooking"} time`,
          action: "SET",
          selectedHr: hr,
          selectedMin: min,
        },
      } ).then( ( result ) => {
        if ( result ) {
          this.recipeContent[ time ] = result
          this.modalOpen = false
          if ( focus ) {
            switch ( time ) {
              case "prepTime":
                this.autoFocusField( "cookTime", false )
                break
              case "cookTime":
                this.autoFocusField( "yieldQuantity", true )
                break
              default:
                break
            }
          }
        }
      } )
    },
    onScroll( args ) {
      this.viewIsScrolled = args.scrollY ? true : false
    },
    // DATA LIST
    showCuisine( focus ) {
      this.modalOpen = true
      this.releaseBackEvent()
      this.$showModal( ActionDialog, {
        props: {
          title: "Cuisine",
          list: this.cuisines,
          stretch: true,
          action: "ADD NEW",
        },
      } ).then( ( action ) => {
        if ( action == "ADD NEW" ) {
          this.$showModal( PromptDialog, {
            props: {
              title: "New cuisine",
              action: "ADD",
            },
          } ).then( ( item ) => {
            this.hijackBackEvent()
            if ( item.length ) {
              this.recipeContent.cuisine = item
              ApplicationSettings.setString( "previousCuisine", item )
              this.addListItemAction( {
                item,
                listName: 'cuisines'
              } )
              this.modalOpen = false
              if ( focus ) this.autoFocusField( "category", false )
            }
          } )
        } else if ( action ) {
          this.recipeContent.cuisine = action
          ApplicationSettings.setString( "previousCuisine", action )
          this.hijackBackEvent()
          this.modalOpen = false
          if ( focus ) this.autoFocusField( "category", false )
        } else {
          this.cuisines.includes( this.recipeContent.cuisine ) ? mull : this.recipeContent.cuisine = 'Undefined'
          this.hijackBackEvent()
        }
      } )
    },
    showCategories( focus ) {
      this.modalOpen = true
      this.releaseBackEvent()
      this.$showModal( ActionDialog, {
        props: {
          title: "Category",
          list: this.categories,
          stretch: true,
          action: "ADD NEW",
        },
      } ).then( ( action ) => {
        if ( action == "ADD NEW" ) {
          this.$showModal( PromptDialog, {
            props: {
              title: "New category",
              action: "ADD",
            },
          } ).then( ( item ) => {
            this.hijackBackEvent()
            if ( item.length ) {
              this.recipeContent.category = item
              ApplicationSettings.setString( "previousCategory", item )
              this.addListItemAction( {
                item,
                listName: 'categories'
              } )
              this.modalOpen = false
              if ( focus ) this.autoFocusField( "tags", true )
            }
          } )
        } else if ( action ) {
          this.recipeContent.category = action
          ApplicationSettings.setString( "previousCategory", action )
          this.hijackBackEvent()
          this.modalOpen = false
          if ( focus ) this.autoFocusField( "tags", true )
        } else {
          this.categories.includes( this.recipeContent.category ) ? mull : this.recipeContent.category = 'Undefined'
          this.hijackBackEvent()
        }
      } )
    },
    showYieldUnits( focus ) {
      this.modalOpen = true
      this.releaseBackEvent()
      this.$showModal( ActionDialog, {
        props: {
          title: "Yield measured in",
          list: this.yieldUnits,
          stretch: true,
          action: "ADD NEW",
        },
      } ).then( ( action ) => {
        if ( action == "ADD NEW" ) {
          this.$showModal( PromptDialog, {
            props: {
              title: "New yield unit",
              action: "ADD",
            },
          } ).then( ( item ) => {
            this.hijackBackEvent()
            if ( item.length ) {
              this.recipeContent.yield.unit = item
              ApplicationSettings.setString( "previousYieldUnit", item )
              this.addListItemAction( {
                item,
                listName: 'yieldUnits'
              } )
              this.modalOpen = false
              if ( focus ) this.autoFocusField( "difficultyLevel", false )
            }
          } )
        } else if ( action ) {
          this.recipeContent.yield.unit = action
          ApplicationSettings.setString( "previousYieldUnit", action )
          this.hijackBackEvent()
          this.modalOpen = false
          if ( focus ) this.autoFocusField( "difficultyLevel", false )
        } else {
          this.yieldUnits.includes( this.recipeContent.yield.unit ) ? mull : this.recipeContent.yield.unit = 'Serving'
          this.hijackBackEvent()
        }
      } )
    },
    showDifficultyLevel( focus ) {
      this.modalOpen = true
      this.releaseBackEvent()
      this.$showModal( ActionDialog, {
        props: {
          title: "Difficulty level",
          list: this.difficultyLevels,
          stretch: false,
        },
      } ).then( ( action ) => {
        if ( action ) {
          this.recipeContent.difficulty = action
          this.hijackBackEvent()
          this.modalOpen = false
          if ( focus ) this.addIngredient()
        } else {
          this.difficultyLevels.includes( this.recipeContent.difficulty ) ? mull : this.recipeContent.difficulty = 'Easy'
          this.hijackBackEvent()
        }
      } )
    },
    showUnits( e, focus, index ) {
      this.modalOpen = true
      this.releaseBackEvent()
      this.$showModal( ActionDialog, {
        props: {
          title: "Units",
          list: this.units,
          stretch: true,
          action: "ADD NEW",
        },
      } ).then( ( action ) => {
        this.hijackBackEvent()
        if ( action == "ADD NEW" ) {
          this.$showModal( PromptDialog, {
            props: {
              title: "New unit",
              action: "ADD",
            },
          } ).then( ( item ) => {
            this.hijackBackEvent()
            if ( item.length ) {
              this.recipeContent.ingredients[ index ].unit = item
              this.addListItemAction( {
                item,
                listName: 'units'
              } )
              this.modalOpen = false
              if ( focus && this.recipeContent.ingredients.length - 1 === index )
                this.autoFocusRefField( 'ingredient', index )


            }
          } )
        } else if ( action ) {
          this.recipeContent.ingredients[ index ].unit = action
          this.modalOpen = false
          if ( focus && this.recipeContent.ingredients.length - 1 === index ) this.autoFocusRefField( 'ingredient', index )
        }
      } )
    },
    autoFocusField( ref, showSoftInput ) {
      this.$refs[ ref ].nativeView.focus()
      if ( showSoftInput ) {
        setTimeout( () => {
          Utils.ad.showSoftInput( this.$refs[ ref ].nativeView.android )
        }, 100 )
      }
    },
    autoFocusRefField( ref, index ) {
      this.$refs[ ref ][ index ].nativeView.focus()
      setTimeout( () => {
        Utils.ad.showSoftInput( this.$refs[ ref ][ index ].nativeView.android )
      }, 100 )
    },
    // NAVIGATION HANDLERS
    navigateBackController() {
      if ( this.navigationFromView ) {
        this.$navigateTo( ViewRecipe, {
          props: {
            filterTrylater: this.filterTrylater,
            recipeID: this.recipeID,
          },
          backstackVisible: false,
        } )
      } else this.$navigateBack()
    },
    navigateBack() {
      if ( this.hasChanges ) {
        this.blockModal = true
        this.$showModal( ConfirmDialog, {
          props: {
            title: "Unsaved changes",
            description: localize( "Are you sure you want to discard unsaved changes to this recipe?" ),
            cancelButtonText: "DISCARD",
            okButtonText: "KEEP EDITING",
          },
        } ).then( ( action ) => {
          this.blockModal = false
          if ( action != null && !action ) {
            this.navigateBackController()
            this.releaseBackEvent()
          }
        } )
      } else {
        this.navigateBackController()
        this.releaseBackEvent()
      }
    },
    hijackBackEvent() {
      AndroidApplication.on( AndroidApplication.activityBackPressedEvent, this.backEvent )
    },
    releaseBackEvent() {
      AndroidApplication.off( AndroidApplication.activityBackPressedEvent, this.backEvent )
    },
    backEvent( args ) {
      if ( this.hasChanges && !this.blockModal ) {
        args.cancel = true
        this.navigateBack()
      }
    },
    // DATA HANDLERS
    imageHandler() {
      this.clearEmptyFields()
      if ( this.recipeContent.imageSrc ) {
        this.blockModal = true
        this.$showModal( ConfirmDialog, {
          props: {
            title: "Recipe photo",
            cancelButtonText: "REMOVE",
            okButtonText: "REPLACE PHOTO",
          },
        } ).then( ( action ) => {
          this.blockModal = false
          if ( action ) {
            this.permissionCheck( this.permissionConfirmation, this.imagePicker )
          } else if ( action != null ) {
            this.recipeContent.imageSrc = null
            this.releaseBackEvent()
          }
        } )
      } else {
        this.permissionCheck( this.permissionConfirmation, this.imagePicker )
      }
    },
    permissionConfirmation() {
      return this.$showModal( ConfirmDialog, {
        props: {
          title: "Grant permission",
          description: localize( "EnRecipes requires storage permission in order to set recipe photo." ),
          cancelButtonText: "NOT NOW",
          okButtonText: "CONTINUE",
        },
      } )
    },
    permissionCheck( confirmation, action ) {
      if ( !ApplicationSettings.getBoolean( "storagePermissionAsked", false ) ) {
        confirmation().then( ( e ) => {
          if ( e ) {
            Permissions.request( "photo" ).then( ( status ) => {
              switch ( status[ 0 ] ) {
                case "authorized":
                  action()
                  break
                case "never_ask_again":
                  ApplicationSettings.setBoolean( "storagePermissionAsked", true )
                  break
                case "denied":
                  Toast.makeText( localize( "Permission denied" ) ).show()
                  break
                default:
                  break
              }
            } )
          }
        } )
      } else {
        Permissions.check( "photo" ).then( ( res ) => {
          res[ 0 ] !== "authorized" ? confirmation().then( ( e ) => e && utils.openAppSettingsPage() ) : action()
        } )
      }
    },
    imagePicker() {
      ApplicationSettings.setBoolean( "storagePermissionAsked", true )
      this.cacheImagePath = path.join( knownFolders.temp().path, `${this.getRandomID()}.jpg` )
      Filepicker.create( {
        mode: "single",
        extensions: [ "png", "jpeg", "jpg" ],
      } ).present().then( ( selection ) => {
        let imgPath = selection[ 0 ]
        ImageSource.fromFile( imgPath ).then( ( image ) => {
          ImageCropper.prototype.show( image, {
            width: 1080,
            height: 1080,
          }, {
            hideBottomControls: true,
            toolbarTitle: localize( "Crop photo" ),
            statusBarColor: "#ff5200",
            toolbarTextColor: this.appTheme == "light" ? "#212529" : "#f1f3f5",
            toolbarColor: this.appTheme == "light" ? "#f1f3f5" : "#212529",
            cropFrameColor: "#ff5200",
          } ).then( ( cropped ) => {
            cropped.image.saveToFile( this.cacheImagePath, "jpg", 75 )
            this.recipeContent.imageSrc = this.cacheImagePath
          } )
        } )
      } )
    },
    // INPUT FIELD HANDLERS
    splitTags() {
      let string
      if ( this.tags ) {
        let tags = this.tags.split( " " ).map( e => {
          string = e.replace( /^[^\w\s]+/, '' )
          if ( /^[A-Za-z]+/.test( string ) ) {
            return string.charAt( 0 ).toUpperCase() + string.slice( 1 )
          }
        } ).filter( e => e )
        this.recipeContent.tags = tags
      }
    },
    joinTags() {
      this.tags = this.recipeContent.tags.join( " " )
    },
    fieldDeletionConfirm( title ) {
      return this.$showModal( ConfirmDialog, {
        props: {
          title,
          cancelButtonText: "CANCEL",
          okButtonText: "REMOVE",
        },
      } )
    },
    addIngredient() {
      let ingredients = this.recipeContent.ingredients
      let unit = ingredients.length ? ingredients[ ingredients.length - 1 ].unit : "unit"
      this.recipeContent.ingredients.push( {
        item: "",
        quantity: null,
        unit,
      } )
    },
    removeIngredient( index ) {
      this.modalOpen = true
      if ( this.recipeContent.ingredients[ index ].item.length ) {
        this.fieldDeletionConfirm( "Remove ingredient?" ).then( ( res ) => {
          if ( res ) {
            this.recipeContent.ingredients.splice( index, 1 )
          }
        } )
      } else {
        this.recipeContent.ingredients.splice( index, 1 )
      }
      setTimeout( e => this.modalOpen = false, 200 )
    },
    addInstruction() {
      this.recipeContent.instructions.push( "" )
    },
    removeInstruction( index ) {
      if ( this.recipeContent.instructions[ index ].length ) {
        this.fieldDeletionConfirm( "Remove instruction?" ).then( ( res ) => {
          res && this.recipeContent.instructions.splice( index, 1 )
        } )
      } else this.recipeContent.instructions.splice( index, 1 )
    },
    getCombinationTitle( id ) {
      return this.recipes.filter( ( e ) => e.id === id )[ 0 ].title
    },
    showCombinations() {
      let existingCombinations = [ ...this.recipeContent.combinations,
        this.recipeContent.id,
      ]
      let filteredRecipes = this.recipes.filter(
        ( e ) => !existingCombinations.includes( e.id ) )
      this.$showModal( ActionDialogWithSearch, {
        props: {
          title: "Select a recipe",
          recipes: filteredRecipes,
        },
      } ).then( ( res ) => {
        if ( res ) {
          this.recipeContent.combinations.push( res )
        }
      } )
    },
    removeCombination( id ) {
      let index = this.recipeContent.combinations.indexOf( id )
      this.fieldDeletionConfirm( "Remove combination?" ).then( res => {
        if ( res ) {
          this.recipeContent.combinations.splice( index, 1 )
          this.unSyncCombinations.push( id )
        }
      } )
    },
    addNote() {
      this.recipeContent.notes.push( "" )
    },
    removeNote( index ) {
      if ( this.recipeContent.notes[ index ].length ) {
        this.fieldDeletionConfirm( "Remove note?" ).then( ( res ) => {
          if ( res ) this.recipeContent.notes.splice( index, 1 )
        } )
      } else this.recipeContent.notes.splice( index, 1 )
    },
    // SAVE OPERATION
    clearEmptyFields() {
      if ( !this.recipeContent.title ) this.recipeContent.title = localize( "Untitled Recipe" )
      if ( !this.recipeContent.yield.quantity ) this.recipeContent.yield.quantity = 1
      this.recipeContent.ingredients = this.recipeContent.ingredients.filter(
        ( e ) => e.item )
      let vm = this

      function clearEmpty( arr ) {
        vm.recipeContent[ arr ] = vm.recipeContent[ arr ].filter( ( e ) => e )
      }
      clearEmpty( "instructions" )
      clearEmpty( "notes" )
    },
    saveOperation() {
      this.saving = this.modalOpen = true
      this.clearEmptyFields()
      this.recipeContent.lastModified = new Date()
      if ( this.cacheImagePath ) {
        let recipeImage = path.join( knownFolders.documents().getFolder( "EnRecipes" ).getFolder( "Images" ).path, `${this.getRandomID()}.jpg` )
        let binarySource = File.fromPath( this.cacheImagePath ).readSync()
        File.fromPath( recipeImage ).writeSync( binarySource )
        this.recipeContent.imageSrc = recipeImage
        knownFolders.temp().clear()
      }
      if ( this.recipeContent.imageSrc ) {
        if ( this.tempRecipeContent.imageSrc && this.tempRecipeContent.imageSrc !== this.recipeContent.imageSrc ) {
          getFileAccess().deleteFile( this.tempRecipeContent.imageSrc )
        }
      } else if ( this.tempRecipeContent.imageSrc ) {
        getFileAccess().deleteFile( this.tempRecipeContent.imageSrc )
      }
      this.unSyncCombinationsAction( {
        id: this.recipeID,
        combinations: this.unSyncCombinations,
      } )
      this.saveRecipe()
    },
    saveRecipe() {
      if ( this.recipeID ) {
        this.overwriteRecipeAction( {
          id: this.recipeID,
          recipe: this.recipeContent,
        } )
      } else {
        this.recipeContent.id = this.newRecipeID
        this.addRecipeAction( {
          id: this.newRecipeID,
          recipe: this.recipeContent,
        } )
      }
      setTimeout( () => {
        this.saving = false
      }, 100 )
      this.navigateBackController()
    },
  },
  created() {
    setTimeout( ( e ) => {
      this.setCurrentComponentAction( "EditRecipe" )
    }, 500 )
    this.title = this.recipeID ? "Edit recipe" : "New recipe"
    if ( this.recipeID ) {
      let recipe = this.recipes.filter( ( e ) => e.id === this.recipeID )[ 0 ]
      Object.assign( this.recipeContent, JSON.parse( JSON.stringify( recipe ) ) )
      Object.assign( this.tempRecipeContent, JSON.parse( JSON.stringify( this.recipeContent ) ) )
      if ( this.recipeContent.tags.length ) this.joinTags()
    } else {
      this.recipeContent.cuisine = this.selectedCuisine ? /All/.test( this.selectedCuisine ) ? "Undefined" : this.selectedCuisine : ApplicationSettings.getString( "previousCuisine", "Undefined" )
      this.recipeContent.category = this.selectedCategory ? /All/.test( this.selectedCategory ) ? "Undefined" : this.selectedCategory : ApplicationSettings.getString( "previousCategory", "Undefined" )
      if ( this.selectedTag && !/All/.test( this.selectedTag ) ) {
        this.tags = this.selectedTag
        this.splitTags()
      }
      this.recipeContent.yield.unit = ApplicationSettings.getString( "previousYieldUnit", "Serving" )
      if ( this.filterFavourites ) this.recipeContent.isFavorite = true
      if ( this.filterTrylater ) this.recipeContent.tried = false
      this.recipeContent.created = new Date()
      Object.assign( this.tempRecipeContent, JSON.parse( JSON.stringify( this.recipeContent ) ) )
      this.newRecipeID = this.getRandomID()
    }
    this.hijackBackEvent()
  },
}
</script>
