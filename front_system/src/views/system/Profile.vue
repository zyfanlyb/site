<template>
  <div class="profile-page">
    <div class="profile-header">
      <div>
        <h2 class="profile-title">个人中心</h2>
      </div>
    </div>
    <a-row :gutter="[24, 24]" class="profile-body">
      <a-col :xs="24" :md="8" class="avatar-section">
        <div class="panel">
          <div class="avatar-wrapper">
            <a-avatar :size="104" :src="avatarUrl">
              <template #icon v-if="!avatarUrl">
                <UserOutlined/>
              </template>
            </a-avatar>
          </div>
          <a-upload
              :show-upload-list="false"
              :before-upload="handleBeforeUpload"
              :custom-request="handleUpload"
              accept="image/*"
          >
            <a-button type="primary" :loading="uploading" class="upload-btn">
              更换头像
            </a-button>
          </a-upload>
        </div>
      </a-col>
      <a-col :xs="24" :md="16" class="info-section">
        <div class="panel">
          <a-tabs class="profile-tabs" default-active-key="profile">
            <a-tab-pane key="profile" tab="基本信息">
              <a-form ref="profileFormRef" layout="vertical" :model="form" :rules="profileRules" class="info-form">
                <a-form-item label="登录账号" name="username">
                  <a-input v-model:value="form.username" disabled/>
                </a-form-item>
                <a-form-item label="昵称" name="nickname"  :rules="[{ required: true, message: '请输入昵称' }]">
                  <a-input v-model:value="form.nickname"/>
                </a-form-item>
                <a-form-item label="邮箱" name="email">
                  <a-input v-model:value="form.email"/>
                </a-form-item>
                <a-form-item v-if="emailChanged" label="邮箱验证码" name="emailCode">
                  <div class="email-code-row">
                    <a-input v-model:value="emailCode" style="flex: 1"/>
                    <a-button
                      :disabled="emailCodeSending || emailCodeCountdown > 0 || !form.email"
                      :loading="emailCodeSending"
                      @click="sendEmailUpdateCode"
                    >
                      {{ emailCodeCountdown > 0 ? `${emailCodeCountdown}s` : '发送验证码' }}
                    </a-button>
                  </div>
                </a-form-item>
                <a-form-item label="手机号" name="phone">
                  <a-input v-model:value="form.phone"/>
                </a-form-item>
                <a-form-item class="form-actions">
                  <a-button type="primary" :loading="saving" class="save-btn" @click="handleSaveInfo">
                    保存修改
                  </a-button>
                </a-form-item>
              </a-form>
            </a-tab-pane>
            <a-tab-pane key="password" tab="修改密码">
              <a-form
                  ref="passwordFormRef"
                  layout="vertical"
                  :model="passwordForm"
                  :rules="passwordRules"
                  class="info-form password-form"
              >
                <a-form-item label="当前密码" name="oldPassword">
                  <a-input-password
                      v-model:value="passwordForm.oldPassword"
                      placeholder="请输入当前密码"
                      autocomplete="current-password"
                  />
                </a-form-item>
                <a-form-item label="新密码" name="newPassword">
                  <a-input-password
                      v-model:value="passwordForm.newPassword"
                      placeholder="至少 6 位，建议包含字母和数字"
                      autocomplete="new-password"
                  />
                </a-form-item>
                <a-form-item label="确认新密码" name="confirmPassword">
                  <a-input-password
                      v-model:value="passwordForm.confirmPassword"
                      placeholder="请再次输入新密码"
                      autocomplete="new-password"
                  />
                </a-form-item>
                <a-form-item class="form-actions">
                  <a-button type="primary" :loading="changingPassword" class="save-btn" @click="handleChangePassword">
                    更新密码
                  </a-button>
                </a-form-item>
              </a-form>
            </a-tab-pane>
          </a-tabs>
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import {ref, watchEffect, onMounted, computed, onUnmounted} from 'vue';
import {UserOutlined} from '@ant-design/icons-vue';
import {message} from 'ant-design-vue';
import type {FormInstance} from 'ant-design-vue';
import {useAuthStore} from '@/stores/auth';
import {storeToRefs} from 'pinia';
import service from '@/utils/request';
import {getFilePreviewUrl} from '@/utils/file';

const authStore = useAuthStore();
const {userInfo} = storeToRefs(authStore);

const avatarUrl = ref('');
const uploading = ref(false);
const saving = ref(false);
const changingPassword = ref(false);
const profileFormRef = ref<FormInstance>();
const passwordFormRef = ref<FormInstance>();

// 基本信息表单
const form = ref({
  userId: undefined as number | undefined,
  username: '',
  nickname: '',
  email: '',
  phone: ''
});

const originalEmail = ref('');
const emailCode = ref('');
const emailCodeSending = ref(false);
const emailCodeCountdown = ref(0);
let emailCodeTimer: any = null;

const emailChanged = computed(() => {
  const now = (form.value.email || '').trim();
  const old = (originalEmail.value || '').trim();
  return now !== old;
});

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

const profileRules = {
  email: [
    {
      validator: async (_rule: any, value: string) => {
        const nextValue = (value || '').trim();
        if (!nextValue) {
          return Promise.resolve();
        }
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (emailRegex.test(nextValue)) {
          return Promise.resolve();
        }
        return Promise.reject('邮箱格式不正确');
      },
      trigger: ['blur', 'change']
    }
  ],
  phone: [
    {
      validator: async (_rule: any, value: string) => {
        const nextValue = (value || '').trim();
        if (!nextValue) {
          return Promise.resolve();
        }
        const phoneRegex = /^1[3-9]\d{9}$/;
        if (phoneRegex.test(nextValue)) {
          return Promise.resolve();
        }
        return Promise.reject('手机号格式不正确');
      },
      trigger: ['blur', 'change']
    }
  ]
};

const passwordRules = {
  oldPassword: [
    {required: true, message: '请输入当前密码', trigger: ['blur', 'change']}
  ],
  newPassword: [
    {required: true, message: '请输入新密码', trigger: ['blur', 'change']},
    {min: 6, message: '新密码长度不能少于 6 位', trigger: ['blur', 'change']},
    {
      validator: async (_rule: any, value: string) => {
        const newPassword = (value || '').trim();
        const oldPassword = (passwordForm.value.oldPassword || '').trim();
        const confirmPassword = (passwordForm.value.confirmPassword || '').trim();
        if (!newPassword) {
          return Promise.resolve();
        }
        if (oldPassword && newPassword === oldPassword) {
          return Promise.reject('新密码不能与当前密码相同');
        }
        return Promise.resolve();
      },
      trigger: ['blur', 'change']
    }
  ],
  confirmPassword: [
    {required: true, message: '请再次输入新密码', trigger: ['blur', 'change']},
    {
      validator: async (_rule: any, value: string) => {
        const confirmPassword = (value || '').trim();
        const newPassword = (passwordForm.value.newPassword || '').trim();
        if (!confirmPassword) {
          return Promise.resolve();
        }
        if (confirmPassword !== newPassword) {
          return Promise.reject('两次输入的新密码不一致');
        }
        return Promise.resolve();
      },
      trigger: ['blur', 'change']
    }
  ]
};

// 加载当前用户详细信息（/user/info -> AuthClient /auth/user/open/info）
const loadUserInfo = async () => {
  const res = await service.post('/user/info');
  const data = res.data;
  form.value.userId = data.userId;
  form.value.username = data.username || '';
  form.value.nickname = data.nickname || '';
  form.value.email = data.email || '';
  form.value.phone = data.phone || '';
  originalEmail.value = data.email || '';
  emailCode.value = '';
  emailCodeCountdown.value = 0;
  if (emailCodeTimer) {
    clearInterval(emailCodeTimer);
    emailCodeTimer = null;
  }
  // 同步到全局 store，保证头像/昵称一致
  await authStore.fetchUserInfo();
};

onMounted(async () => {
  await loadUserInfo();
});

onUnmounted(() => {
  if (emailCodeTimer) {
    clearInterval(emailCodeTimer);
    emailCodeTimer = null;
  }
});

// 同 Home.vue，一样根据 userInfo.avatar 生成预览 URL
watchEffect(async () => {
  if (userInfo.value?.avatar) {
    avatarUrl.value = await getFilePreviewUrl(userInfo.value.avatar);
  } else {
    avatarUrl.value = '';
  }
});

const handleBeforeUpload = (file: File) => {
  const isImage = file.type.startsWith('image/');
  if (!isImage) {
    message.error('只能上传图片文件');
    return false;
  }
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isLt2M) {
    message.error('图片大小不能超过 2MB');
    return false;
  }
  return true;
};

const handleUpload = async (options: any) => {
  const {file, onSuccess, onError} = options;
  uploading.value = true;
  try {
    const formData = new FormData();
    formData.append('avatar', file as Blob);
    const res = await service.post('/user/updateAvatar', formData, {
      headers: {'Content-Type': 'multipart/form-data'}
    });
    // 后端返回成功后，前端可以重新拉一次用户信息，保证头像地址最新
    await authStore.fetchUserInfo();
    message.success('头像更新成功');
    onSuccess(res);
  } catch (e) {
    console.error(e);
    message.error('头像更新失败');
    onError(e);
  } finally {
    uploading.value = false;
  }
};

// 保存基本信息（/user/update -> AuthClient /auth/user/open/updateInfo）
const handleSaveInfo = async () => {
  try {
    await profileFormRef.value?.validate();
  } catch {
    return;
  }

  const email = (form.value.email || '').trim();
  const phone = (form.value.phone || '').trim();

  saving.value = true;
  try {
    let emailVerifyTicket = '';
    if (emailChanged.value && email) {
      const code = (emailCode.value || '').trim();
      if (!/^\d{6}$/.test(code)) {
        message.error('请输入 6 位邮箱验证码');
        return;
      }
      const verifyRes = await service.post('/user/verify/email/verifyCode', {
        data: { email, code, scene: 'updateEmail' }
      });
      emailVerifyTicket = verifyRes.data;
    }
    await service.post('/user/updateInfo', {
      ...form.value,
      email,
      phone,
      emailVerifyTicket
    });
    message.success('保存成功');
    await authStore.fetchUserInfo();
    originalEmail.value = email;
    emailCode.value = '';
  } catch (e) {
    console.error(e);
    message.error('保存失败');
  } finally {
    saving.value = false;
  }
};

const sendEmailUpdateCode = async () => {
  const email = (form.value.email || '').trim();
  if (!email) {
    message.error('请输入邮箱');
    return;
  }
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email)) {
    message.error('邮箱格式不正确');
    return;
  }
  try {
    emailCodeSending.value = true;
    await service.post('/user/verify/email/sendCode', {
      data: { email, title: '修改邮箱验证码', scene: 'updateEmail' }
    });
    message.success('验证码已发送，请查收邮箱');
    emailCodeCountdown.value = 60;
    if (emailCodeTimer) clearInterval(emailCodeTimer);
    emailCodeTimer = setInterval(() => {
      emailCodeCountdown.value -= 1;
      if (emailCodeCountdown.value <= 0) {
        clearInterval(emailCodeTimer);
        emailCodeTimer = null;
      }
    }, 1000);
  } finally {
    emailCodeSending.value = false;
  }
};

const resetPasswordForm = () => {
  passwordForm.value.oldPassword = '';
  passwordForm.value.newPassword = '';
  passwordForm.value.confirmPassword = '';
  passwordFormRef.value?.clearValidate();
};

const handleChangePassword = async () => {
  try {
    await passwordFormRef.value?.validate();
  } catch {
    return;
  }

  const oldPassword = (passwordForm.value.oldPassword || '').trim();
  const newPassword = (passwordForm.value.newPassword || '').trim();

  changingPassword.value = true;
  try {
    await service.post('/user/updatePassword', {oldPassword, newPassword});
    message.success('密码修改成功');
    resetPasswordForm();
    const savedRedirectUrl = authStore.redirectUrl;
    authStore.clearToken();
    if (savedRedirectUrl) {
      window.location.href = `${savedRedirectUrl}?logout=true`;
    } else {
      const url = `${(import.meta.env.VITE_LOGIN_DOMAIN || window.location.origin)}/auth/login?redirectUrl=${encodeURIComponent(window.location.origin + import.meta.env.VITE_PRE_PATH + '/login')}`;
      window.location.href = url;
    }
  } catch (e) {
    console.error(e);
    message.error('密码修改失败');
  } finally {
    changingPassword.value = false;
  }
};
</script>

<style scoped>
.profile-page {
  padding: 12px 16px 16px;
  min-height: 100%;
  background: transparent;
}

.profile-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
  padding: 4px 2px 0;
}

.profile-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #0f172a;
}

.profile-subtitle {
  margin-top: 4px;
  margin-bottom: 0;
  font-size: 13px;
  color: #64748b;
}

.profile-body {
  margin: 0;
  padding: 20px;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  background: #fff;
}

.avatar-section {
  text-align: center;
  border-right: 1px solid #f0f0f0;
  padding-right: 24px;
}

.panel {
  padding: 0;
  border-radius: 0;
  background: transparent;
  border: none;
  box-shadow: none;
}

.avatar-wrapper {
  margin: 18px 0 14px;
}

.avatar-wrapper :deep(.ant-avatar) {
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.12);
}

.upload-btn {
  min-width: 110px;
  border-radius: 8px;
  height: 36px;
  font-weight: 500;
}

.tip {
  margin-top: 10px;
  color: #94a3b8;
  font-size: 12px;
}

.info-section {
  padding-left: 24px;
}

.section-desc {
  margin: 0 0 14px;
  color: #64748b;
  font-size: 13px;
}

.info-form {
  max-width: 560px;
}

.profile-tabs :deep(.ant-tabs-nav) {
  margin-bottom: 16px;
}

.profile-tabs :deep(.ant-tabs-tab) {
  padding: 8px 0;
}

.profile-tabs :deep(.ant-tabs-tab + .ant-tabs-tab) {
  margin-left: 24px;
}

.profile-tabs :deep(.ant-tabs-tab-btn) {
  font-weight: 600;
}

.info-form :deep(.ant-form-item-label > label) {
  color: #334155;
  font-weight: 600;
}

.info-form :deep(.ant-input) {
  border-radius: 8px;
  min-height: 36px;
}

.info-form :deep(.ant-input[disabled]) {
  color: #64748b;
  background: #f8fafc;
}

.form-actions {
  margin-bottom: 0;
  padding-top: 6px;
}

.save-btn {
  min-width: 120px;
  border-radius: 8px;
  height: 36px;
  font-weight: 500;
}

.email-code-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.password-form {
  margin-bottom: 2px;
}

@media (max-width: 767px) {
  .profile-page {
    padding: 8px 10px 12px;
  }

  .profile-header {
    padding: 2px 0;
    align-items: flex-start;
    gap: 10px;
  }

  .profile-title {
    font-size: 18px;
  }

  .profile-body {
    padding: 14px;
  }

  .avatar-section {
    border-right: none;
    border-bottom: 1px solid #f0f0f0;
    padding-right: 0;
    padding-bottom: 16px;
    margin-bottom: 2px;
  }

  .info-section {
    padding-left: 0;
    padding-top: 10px;
  }

  .panel {
    padding: 0;
  }
}
</style>

