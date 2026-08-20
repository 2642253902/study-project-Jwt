<template>
    <div style="text-align: center; margin: 0 20px;">
        <div style="margin-top: 100px;">
            <div style="font-size: 25px; font-weight: bold;">注册新用户</div>
            <div style="font-size: 14px; color: grey;">欢迎注册我们的学习平台，请在下方填写相关信息</div>
        </div>
        <div style="margin-top: 50px;">
            <el-form :model="form" :rules="rule" ref="formRef">
                <el-form-item prop="username">
                    <el-input v-model="form.username" maxlength="10" type="text" placeholder="用户名">
                        <template #prefix>
                            <el-icon>
                                <User />
                            </el-icon>
                        </template>
                    </el-input>
                </el-form-item>
                <el-form-item prop="password">
                    <el-input v-model="form.password" maxlength="20" placeholder="密码" type="password">
                        <template #prefix>
                            <el-icon>
                                <Lock />
                            </el-icon>
                        </template>
                    </el-input>
                </el-form-item>
                <el-form-item prop="password_repeat">
                    <el-input v-model="form.password_repeat" maxlength="20" placeholder="再次输入密码" type="password">
                        <template #prefix>
                            <el-icon>
                                <Lock />
                            </el-icon>
                        </template>
                    </el-input>
                </el-form-item>
                <el-form-item prop="email">
                    <el-input v-model="form.email" maxlength="30" placeholder="邮箱">
                        <template #prefix>
                            <el-icon>
                                <Message />
                            </el-icon>
                        </template>
                    </el-input>
                </el-form-item>
                <el-form-item prop="code">
                    <el-row :gutter="10" style="width:100%">
                        <el-col :span="17">
                            <el-input v-model="form.code" maxlength="6" placeholder="验证码">
                                <template #prefix>
                                    <el-icon>
                                        <EditPen />
                                    </el-icon>
                                </template>
                            </el-input>
                        </el-col>
                        <el-col :span="5">
                            <el-button type="success" @click="asCode" :disabled="!isEmailValid || coldTime > 0">
                                获取验证码</el-button>
                        </el-col>
                    </el-row>
                </el-form-item>
            </el-form>
        </div>
        <div style="margin-top: 80px;">
            <el-button style="width: 270px" type="warning" plain @click="register">立即注册</el-button>
        </div>
        <div style="margin-top: 20px;">
            <span style="font-size: 14px; line-height: 15px; color:grey">已有账号？</span>
            <el-link style="translate: 0 -1px " @click="router.push('/')" type="primary">立即登录</el-link>
        </div>
    </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { EditPen, Lock, Message, User } from '@element-plus/icons-vue'
import router from '@/router'
import { get, post } from '@/net';
import { ElMessage } from 'element-plus';

const formRef = ref();
const coldTime = ref(0);



const form = reactive({
    username: '',
    password: '',
    password_repeat: '',
    email: '',
    code: '',
})

const validateUsername = (rule, value, callback) => {
    if (!value) {
        callback(new Error('请输入用户名'))
    } else if (!/^[a-zA-Z0-9\u4e00-\u9fa5]+$/.test(value)) {
        callback(new Error('用户名只能包含字母、数字和中文字符'))
    } else {
        callback()
    }
}

const validatePassword = (rule, value, callback) => {
    if (!value) {
        callback(new Error('请输入密码'))
    } else if (value !== form.password) {
        callback(new Error('两次密码输入不一致'))
    } else {
        callback()
    }
}

const rule = reactive({
    username: [
        { validator: validateUsername, trigger: ['blur', "change"] }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度必须在6到20个字符之间', trigger: ['blur', 'change'] }
    ],
    password_repeat: [
        { validator: validatePassword, trigger: ['blur', "change"] },
        { required: true, message: '请再次输入密码', trigger: 'blur' }
    ],
    email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入有效的邮箱地址', trigger: ['blur', 'change'] }
    ],
    code: [
        { required: true, message: '请输入验证码', trigger: 'blur' }
    ]
})

const isEmailValid = computed(() => {
    return /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(form.email);
});


let countdownTimer = null;

function asCode() {
    if (coldTime.value > 0) return;

    if (isEmailValid.value) {
        coldTime.value = 60;
        if (countdownTimer) clearInterval(countdownTimer);

        countdownTimer = setInterval(() => {
            coldTime.value--;
            if (coldTime.value <= 0) {
                coldTime.value = 0;
                clearInterval(countdownTimer);
                countdownTimer = null;
            }
        }, 1000);
    } else {
        ElMessage.error('请输入有效的邮箱地址');
    }
}


function register() {
    formRef.value.validate((valid) => {
        if (valid) {
            post(`/api/auth/register`, { ...form }, () => {
                ElMessage.success('注册成功，请登录')
                router.push('/')
            },)
        } else {
            ElMessage.error('请检查输入信息是否正确')
        }
    })
}


</script>

<style></style>