const passwordInput = document.getElementById("password");
const confirmInput = document.getElementById("confirm");
const showPasswordCheckbox = document.getElementById("showPasswordCheckbox");
const passwordValidate = document.getElementById("confirm")
showPasswordCheckbox.addEventListener("change", function () {
    if (showPasswordCheckbox.checked) {
        passwordInput.type = "text";
        confirmInput.type = "text";
    } else {
        passwordInput.type = "password";
        confirmInput.type = "password";
    }
});
passwordValidate.addEventListener("focusin", function () {
    passwordValidate.className = "form-control text-bg-dark";
});

function validateForm() {
    let password = document.getElementById("password").value;
    let confirmPassword = document.getElementById("confirm").value;
    if (password !== confirmPassword) {
        passwordValidate.className = "form-control is-invalid text-bg-dark";
        return false;
    }
}