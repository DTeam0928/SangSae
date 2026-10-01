// Header Active
// 선택된 메뉴를 강조한다
document.addEventListener("DOMContentLoaded", function() {
const currentPath = window.location.pathname;

const navHome = document.getElementById("nav-home");
const navQuest = document.getElementById("nav-quest");
const navRoutine = document.getElementById("nav-routine");

document.querySelectorAll(".nav a").forEach(el => el.classList.remove("active"));

if (currentPath.includes("quest")) {
if(navQuest) navQuest.classList.add("active");
} else if (currentPath.includes("routine")) {
if(navRoutine) navRoutine.classList.add("active");
} else {
if(navHome) navHome.classList.add("active");
}
});