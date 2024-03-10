import {type Writable, writable} from "svelte/store";

export interface TNotification {
    title: string;
    message: string;
    error: boolean;
    stay: boolean | undefined;
}

export const notification: Writable<TNotification | null> = writable(null);