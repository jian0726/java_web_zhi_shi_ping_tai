import { defineStore } from 'pinia'
import { ref } from 'vue'

export const usePermissionStore = defineStore('permission', () => {
  const roles = ref<string[]>([])

  function setRoles(value: string[]) {
    roles.value = value
  }

  function hasRole(role: string) {
    return roles.value.includes(role)
  }

  return { roles, setRoles, hasRole }
})
