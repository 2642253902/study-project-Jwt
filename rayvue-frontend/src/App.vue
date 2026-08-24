<script setup>
import { useDark, useToggle } from '@vueuse/core';

useDark({
    selector: 'html',
    attr: 'class',
    valueDark: 'dark',
    valueLight: 'light',
})

useDark({
    onChanged(dark) {
        useToggle(dark, 'dark', 'light')
    },
})

</script>

<template>
    <div>
        <router-view />
    </div>
</template>

<style scoped></style>
