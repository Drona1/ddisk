let menuVisible = false;
document.addEventListener("contextmenu", function (event) {
    event.preventDefault();
    let target = event.target;
    if (target.tagName === "TD") {
        showTableMenu();
    }
});
document.addEventListener("click", function (event) {
    if (menuVisible && event.target.tagName !== "BUTTON") {
        tableMenu.style.display = "none";
        tableMenu.style.display = "";
        menuVisible = false;
    }
});

function selectRow(row, contextmenuFlag) {
    let checkboxes = document.querySelectorAll('input[type=checkbox]');
    let checkbox = row.querySelector('input[type=checkbox]');
    if (contextmenuFlag === false || checkbox.checked === false) {
        for (let i = 0; i < checkboxes.length; i++) {
            checkboxes[i].checked = false;
        }
    }
    checkbox.checked = true;
}

function toggleAllCheckboxes(toggle) {
    const checkboxes = document.querySelectorAll('table input[type="checkbox"]');
    for (let i = 0; i < checkboxes.length; i++) {
        checkboxes[i].checked = !toggle.checked;
    }
    toggle.checked = !toggle.checked;
}

function showTableMenu() {
    const tableMenu = document.querySelector("#tableMenu");
    tableMenu.style.display = "block";
    tableMenu.style.position = "absolute";
    if (event.clientX > screen.width - 180) {
        tableMenu.style.left = screen.width - 180 + "px";
    } else {
        tableMenu.style.left = event.clientX + "px";
    }
    if (event.clientY > screen.height - 260) {
        tableMenu.style.top = screen.height - 350 + "px";
    } else {
        tableMenu.style.top = event.clientY + "px";
    }
    menuVisible = true;
}
