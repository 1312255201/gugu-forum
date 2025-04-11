import {defineStore} from "pinia"
import axios from "axios";
export const useStore = defineStore('general',{
    state: ()=> {
        return{
            user:{
                id: -1,
                username:'',
                email:'',
                role:'',
                avatar: '',
                registerTime:null
            },
            forum: {
                types: []
            }
        }
    },getters:{
        avatarUrl(){
            if(this.user.avatar)
                return `${axios.defaults.baseURL}/images${this.user.avatar}`
            else
                return '/avatar.jpg'
        }
    }, actions: {
        findTypeById(id) {
            for (let type of this.forum.types) {
                if(type.id === id)
                    return type
            }
        }
    }
})