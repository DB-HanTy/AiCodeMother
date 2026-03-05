<template>
  <div id="userProfilePage">
    <h2 class="title">个人中心</h2>
    <a-form
      :model="formState"
      v-bind="layout"
      name="nest-messages"
      :validate-messages="validateMessages"
      @finish="onFinish"
    >
      <a-form-item :name="['user', 'name']" label="用户名" :rules="[{ required: true }]">
        <a-input v-model:value="formState.user.name" />
      </a-form-item>
      <a-form-item :name="['user', 'avatar']" label="头像地址">
        <a-input v-model:value="formState.user.avatar" />
      </a-form-item>
      <a-form-item :name="['user', 'introduction']" label="个人简介">
        <a-textarea v-model:value="formState.user.introduction" />
      </a-form-item>
      <a-form-item label="角色">
        <span>{{ loginUserStore.loginUser.userRole === 'admin' ? '管理员' : '普通用户' }}</span>
      </a-form-item>
      <a-form-item :wrapper-col="{ ...layout.wrapperCol, offset: 8 }">
        <a-button type="primary" html-type="submit">保存</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { reactive, onMounted } from 'vue'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { updateUser } from '@/api/userController.ts'
import { message } from 'ant-design-vue'

const loginUserStore = useLoginUserStore()

const layout = {
  labelCol: { span: 8 },
  wrapperCol: { span: 16 },
}

const validateMessages = {
  required: '${label} is required!',
  types: {
    email: '${label} is not a valid email!',
    number: '${label} is not a valid number!',
  },
  number: {
    range: '${label} must be between ${min} and ${max}',
  },
}

// 本地表单状态，嵌套结构
const formState = reactive({
  user: {
    name: '',
    avatar: '',
    introduction: '',
  },
})

// 初始化表单数据
onMounted(() => {
  // 确保登录用户已加载
  loginUserStore.fetchLoginUser().then(() => {
    const u = loginUserStore.loginUser
    formState.user.name = u.userName || ''
    formState.user.avatar = u.userAvatar || ''
    formState.user.introduction = u.userProfile || ''
  })
})

const onFinish = async (values: any) => {
  const u = loginUserStore.loginUser
  if (!u.id) return

  const updateData: API.UserUpdateRequest = {
    id: u.id,
    userName: values.user.name,
    userAvatar: values.user.avatar,
    userProfile: values.user.introduction,
    userRole: u.userRole,
  }

  try {
    const res = await updateUser(updateData)
    if (res.data.code === 0) {
      message.success('更新成功')
      // 同步 store
      loginUserStore.setLoginUser({ ...u, ...updateData })
    } else {
      message.error('更新失败：' + res.data.message)
    }
  } catch (error) {
    console.error('更新失败', error)
    message.error('更新失败')
  }
}
</script>

<style scoped>
#userProfilePage {
  background: white;
  max-width: 720px;
  padding: 24px;
  margin: 24px auto;
}

.title {
  text-align: center;
  margin-bottom: 16px;
}
</style>