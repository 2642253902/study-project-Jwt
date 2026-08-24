<template>
    <div style="text-align: center;">
        <div style=" margin-top: 30px;">
            <el-steps :active="active" align-center>
                <el-step title="验证电子邮件"></el-step>
                <el-step title="重置密码"></el-step>
            </el-steps>
        </div>
        <div style="margin: 0 20px;" v-if="active === 0">
            <div style="margin-top: 80px">
                <div style="font-size: 25px; font-weight: bold; ">重置密码</div>
                <div style="font-size: 14px; color: grey; ">请输入需要重置密码的电子邮件地址</div>
            </div>
            <div style="margin-top: 50px;">
                <el-form :model="form" rules="rules" ref="formRef">
                    <el-form-item :label-width="formLabelWidth" prop="email">
                        <el-input v-model="form.email" placeholder="请输入电子邮件" type="email">
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
                                    {{ coldTime ? `请稍后${coldTime}秒` : '获取验证码' }}</el-button>
                            </el-col>
                        </el-row>
                    </el-form-item>
                </el-form>
            </div>
            <div style="margin-top: 80px;">
                <el-button style="width: 270px" type="warning" plain @click="confirmReset">开始重置密码</el-button>
            </div>
        </div>
        <div style="margin: 0 20px" v-if="active === 1">
            <div style="margin-top: 80px">
                <div style="font-size: 25px; font-weight: bold; ">重置密码</div>
                <div style="font-size: 14px; color: grey; ">请填写你的新密码，务必牢记，防止丢失</div>
            </div>
            <div style="margin-top: 50px;">
                <el-form :model="form" rules="rules" ref="formRef">
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
                </el-form>
            </div>
            <div style="margin-top: 80px;">
                <el-button style="width: 270px" type="danger " plain @click="doReset">立即重置密码</el-button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { EditPen, Lock, Message, User } from '@element-plus/icons-vue'
import { get, post } from '@/net';
import router from '@/router';

const active = ref(0);
const coldTime = ref(0);
const formRef = ref();

const form = reactive({
    email: '',
    code: '',
    password: '',
    password_repeat: '',
});

const validatePassword = (rule, value, callback) => {
    if (!value) {
        callback(new Error('请输入密码'))
    } else if (value !== form.password) {
        callback(new Error('两次密码输入不一致'))
    } else {
        callback()
    }
}


const rules = {
    email: [
        { required: true, message: '请输入邮箱地址', trigger: 'blur' },
        { type: 'email', message: '请输入有效的邮箱地址', trigger: ['blur', 'change'] }
    ],
    code: [
        { required: true, message: '请输入验证码', trigger: 'blur' }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度为6-20位', trigger: ['blur', 'change'] }
    ],
    password_repeat: [
        { required: true, message: '请再次输入密码', trigger: 'blur' },
        { validator: validatePassword, trigger: ['blur', 'change'] }
    ]
};


const isEmailValid = computed(() => {
    return /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(form.email);
});


function asCode() {
    if (coldTime.value > 0) return;

    if (isEmailValid.value) {
        coldTime.value = 60;
        get(`/api/auth/ask-code?email=${(form.email)}&type=reset`, () => {
            ElMessage.success('验证码已发送，请注意查收')
            setInterval(() => {
                coldTime.value--;
            }, 1000);
        }, (error) => {
            ElMessage.error(error?.response?.data?.message || '发送验证码失败，请稍后再试')
            coldTime.value = 0;
        });

    } else {
        ElMessage.error('请输入有效的邮箱地址');
    }
};

function confirmReset() {
    formRef.value.validate((valid) => {
        if (valid) {
            post('api/auth/reset-confirm', {
                email: form.email,
                code: form.code,
            }, () => active.value++)
        } else {
            ElMessage.error('请检查输入是否正确')
        }
    });
}

function doReset() {
    formRef.value.validate((valid) => {
        if (valid) {
            post('api/auth/reset-password', {
                ...form
            }, () => {
                ElMessage.success('密码重置成功，请重新登录')
                router.push('/')
            })
        } else {
            ElMessage.error('请检查输入是否正确')
        }
    });
}
</script>

<style></style>