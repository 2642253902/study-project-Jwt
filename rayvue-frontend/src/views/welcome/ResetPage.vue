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
                <el-form :model="form">
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
                <el-button style="width: 270px" type="warning" plain @click="active++">开始重置密码</el-button>
            </div>
        </div>
        <div style="margin: 0 20px" v-if="active === 1">
            <div style="margin-top: 80px">
                <div style="font-size: 25px; font-weight: bold; ">重置密码</div>
                <div style="font-size: 14px; color: grey; ">请填写你的新密码，务必牢记，防止丢失</div>
            </div>
            <div style="margin-top: 50px;">
                <el-form :model="form">
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
                <el-button style="width: 270px" type="danger " plain @click="active++">立即重置密码</el-button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { EditPen, Lock, Message, User } from '@element-plus/icons-vue'

const active = ref(0)


const form = reactive({
    email: '',
    code: '',
    password: '',
    password_repeat: '',
})
</script>

<style></style>