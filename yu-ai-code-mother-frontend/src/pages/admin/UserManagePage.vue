<template>
  <div id="userManagePage">
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="账号">
        <a-input v-model:value="searchParams.userAccount" placeholder="输入账号" />
      </a-form-item>
      <a-form-item label="用户名">
        <a-input v-model:value="searchParams.userName" placeholder="输入用户名" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>
    <a-divider />
    <!-- 表格 -->
    <a-table
      :columns="columns"
      :data-source="data"
      :pagination="pagination"
      @change="doTableChange"
    >
      <template #bodyCell="{ column, record }">
        <!-- editable fields: 用户名和简介 -->
        <template v-if="['userName','userProfile'].includes(column.dataIndex)">
          <div>
            <a-input
              v-if="editableData[record.id]"
              v-model:value="editableData[record.id][column.dataIndex]"
              style="margin: -5px 0"
            />
            <template v-else>
              {{ record[column.dataIndex] }}
            </template>
          </div>
        </template>
        <template v-else-if="column.dataIndex === 'userRole'">
          <div v-if="editableData[record.id]">
            <a-select v-model:value="editableData[record.id].userRole" style="width: 120px">
              <a-select-option value="user">普通用户</a-select-option>
              <a-select-option value="admin">管理员</a-select-option>
            </a-select>
          </div>
          <div v-else>
            <div v-if="record.userRole === 'admin'">
              <a-tag color="green">管理员</a-tag>
            </div>
            <div v-else>
              <a-tag color="blue">普通用户</a-tag>
            </div>
          </div>
        </template>
        <template v-else-if="column.dataIndex === 'userAvatar'">
          <a-image :src="record.userAvatar" :width="120" />
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space v-model:value="size">
            <span v-if="editableData[record.id]">
              <a-button type="primary" size="small" @click="save(record.id)">保存</a-button>
              <a-button size="small" @click="cancel(record.id)">取消</a-button>
            </span>
            <span v-else >
              <a-button size="small" @click="edit(record)">编辑</a-button>
              <a-button danger size="small" @click="doDelete(record.id)">删除</a-button>
            </span>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
  
</template>
<script lang="ts" setup>
import { computed, onMounted, reactive, ref, type UnwrapRef } from 'vue'
import { deleteUser, listUserVoByPage, updateUser } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { cloneDeep } from 'lodash-es'

const size = ref(10);

const columns = [
  {
    title: 'id',
    dataIndex: 'id',
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
  },
  {
    title: '用户名',
    dataIndex: 'userName',
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
  },
  {
    title: '用户角色',
    dataIndex: 'userRole',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]

// 展示的数据
const data = ref<API.UserVO[]>([])
const total = ref(0)

// 用来存储正在编辑的行拷贝
const editableData: UnwrapRef<Record<string, API.UserVO>> = reactive({})

// 搜索条件
const searchParams = reactive<API.UserQueryRequest>({
  pageNum: 1,
  pageSize: 10,
})

// 获取数据
const fetchData = async () => {
  const res = await listUserVoByPage({
    ...searchParams,
  })
  if (res.data.data) {
    data.value = res.data.data.records ?? []
    total.value = res.data.data.totalRow ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
}

// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.pageNum ?? 1,
    pageSize: searchParams.pageSize ?? 10,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total: number) => `共 ${total} 条`,
  }
})

// 表格分页变化时的操作
const doTableChange = (page: { current: number; pageSize: number }) => {
  searchParams.pageNum = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 搜索数据
const doSearch = () => {
  // 重置页码
  searchParams.pageNum = 1
  fetchData()
}

// 删除数据
const doDelete = async (id: string) => {
  if (!id) {
    return
  }
  const res = await deleteUser({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    // 刷新数据
    fetchData()
  } else {
    message.error('删除失败')
  }
}

// 开始编辑某行，将记录深拷贝到 editableData
const edit = (record: API.UserVO) => {
  if (record.id) {
    editableData[record.id] = cloneDeep(record)
  }
}

// 保存编辑内容，调用接口更新
const save = async (id: string) => {
  if (!id || !editableData[id]) return
  const updated = editableData[id]
  try {
    const res = await updateUser({
      id: updated.id,
      userName: updated.userName,
      userProfile: updated.userProfile,
      userRole: updated.userRole,
      // 其他字段可按需添加
    })
    if (res.data.code === 0) {
      message.success('保存成功')
      // 更新本地数据
      const idx = data.value.findIndex(item => item.id === id)
      if (idx >= 0) {
        data.value[idx] = { ...data.value[idx], ...editableData[id] }
      }
      delete editableData[id]
    } else {
      message.error('保存失败：' + res.data.message)
    }
  } catch (error) {
    console.error('保存失败：', error)
    message.error('保存失败')
  }
}

// 取消编辑
const cancel = (id: string) => {
  if (editableData[id]) {
    delete editableData[id]
  }
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
#userManagePage {
  padding: 24px;
  background: white;
  margin-top: 16px;
}

.editable-row-operations a {
  margin-right: 8px;
}
</style>
