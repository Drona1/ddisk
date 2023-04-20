let originalTableHTML;
window.onload = function () {
    originalTableHTML = document.getElementById("tableShare").innerHTML;
};

const elDrop = document.getElementById('dragItem');
elDrop.addEventListener('dragover', function (event) {
    event.preventDefault();
});

elDrop.addEventListener('drop', async function (event) {
    event.preventDefault();
    let items = await getAllFileEntries(event.dataTransfer.items);
    await uploadDroppedDir(items);
});

async function uploadDroppedDir(files) {
    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    let currentFolder = document.getElementById("currentFolder").value;
    formData.append("currentFolder", currentFolder);
    for (var i = 0; i < files.length; i++) {
        const file = await getFileFromEntry(files[i]);
        formData.append("files", file);
        formData.append("webkitRelativePaths", files[i].fullPath.substring(1).replace(/\\/g, "/"));
    }
    xhr.open("POST", "/upload");
    xhr.onload = function () {
        if (xhr.status === 200) {
            console.log("Files uploaded successfully");
            location.reload();
        } else {
            console.error("Files upload error");
        }
    };
    xhr.send(formData);
}

async function getAllFileEntries(dataTransferItemList) {
    let fileEntries = [];
    let queue = [];
    for (let i = 0; i < dataTransferItemList.length; i++) {
        queue.push(dataTransferItemList[i].webkitGetAsEntry());
    }
    while (queue.length > 0) {
        let entry = queue.shift();
        if (entry.isFile) {
            fileEntries.push(entry);
        } else if (entry.isDirectory) {
            let reader = entry.createReader();
            queue.push(...await readAllDirectoryEntries(reader));
        }
    }
    return fileEntries;
}

async function readAllDirectoryEntries(directoryReader) {
    let entries = [];
    let readEntries = await readEntriesPromise(directoryReader);
    while (readEntries.length > 0) {
        entries.push(...readEntries);
        readEntries = await readEntriesPromise(directoryReader);
    }
    return entries;
}

async function readEntriesPromise(directoryReader) {
    try {
        return await new Promise((resolve, reject) => {
            directoryReader.readEntries(resolve, reject);
        });
    } catch (err) {
        console.log(err);
    }
}

function getFileFromEntry(fileEntry) {
    return new Promise((resolve, reject) => {
        fileEntry.file(file => {
            resolve(file);
        }, error => {
            reject(error);
        });
    });
}

function downloadFile() {
    document.getElementById("downloadButton").click();
}

const fileUpload = document.getElementById("fileUpload");
const folderUpload = document.getElementById("folderUpload");
$(document).ready(function () {
    if (infoInput != null) {
        uploadFunctions();
    }
});

function uploadFunctions() {
    fileUpload.addEventListener("click", function (event) {
        event.preventDefault();
        let fileInput = document.createElement("input");
        fileInput.type = "file";
        fileInput.multiple = true;
        fileInput.addEventListener("change", function () {
            uploadFiles(this.files);
        });
        fileInput.click();
    });

    folderUpload.addEventListener("click", function (event) {
        event.preventDefault();
        let fileInput = document.createElement("input");
        fileInput.type = "file";
        fileInput.webkitdirectory = true;
        fileInput.addEventListener("change", function () {
            uploadFolder(this.files);
        });
        fileInput.click();
    });
}


async function uploadFolder(files) {
    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    for (let i = 0; i < files.length; i++) {
        formData.append("files", files[i]);
        formData.append("webkitRelativePaths", files[i].webkitRelativePath.replace(/\\/g, "/"));
    }
    let currentFolder = document.getElementById("currentFolder").value;
    formData.append("currentFolder", currentFolder);
    xhr.open("POST", "/upload");
    xhr.onload = function () {
        if (xhr.status === 200) {
            console.log("Folders uploaded successfully");
            location.reload();
        } else {
            console.error("Forders upload error");
        }
    };
    xhr.send(formData);
}

function uploadFiles(files) {
    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    let currentFolder = document.getElementById("currentFolder").value;
    for (let i = 0; i < files.length; i++) {
        formData.append("files", files[i]);
    }
    formData.append("currentFolder", currentFolder);
    xhr.open("POST", "/upload");
    xhr.onload = function () {
        if (xhr.status === 200) {
            console.log("Files uploaded successfully");
            location.reload();
        } else {
            console.error("Files upload error");
        }
    };
    xhr.send(formData);
}

function showGlobalButton(event) {
    const globalButton = document.getElementById("globalButton");
    if (event.target.textContent === "Restricted") {
        globalButton.hidden = true;
        event.target.closest('div').querySelector('button').innerHTML = "Restricted";
        document.getElementById('globalAccessLabel').textContent = "Only people with access can open with the link";
    } else {
        globalButton.hidden = false;
        event.target.closest('div').querySelector('button').innerHTML = "Anyone with the link";
        document.getElementById('globalAccessLabel').textContent = "Anyone can open with the link";
    }
}

function changeButtonName(event, value) {
    event.target.closest('div').querySelector('button').innerHTML = value;
}

function addRow(event) {
    event.preventDefault();
    const shareEmail = document.getElementById("shareEmail");
    if (!shareEmail.value.match(/^[^\s@]+@[^\s@]+\.[^\s@]+$/)) {
        shareEmail.className = "form-control text-bg-dark is-invalid";
        return;
    }
    shareEmail.class = "form-control text-bg-dark";

    const table = document.getElementById("tableShare");
    const row = document.getElementById("trPattern");
    const rowCopy = row.cloneNode(true);
    const accessRight = document.getElementById("accessRight").textContent;
    rowCopy.hidden = false;
    rowCopy.cells[0].textContent = shareEmail.value;
    rowCopy.cells[1].querySelector("button").textContent = accessRight;

    table.appendChild(rowCopy);

}

function removeRow(event) {
    var table = document.getElementById("tableShare");
    event.target.closest('tr').remove();
}

function createShareTable() {
    const address = $("input:checked").get()[0].value;
    let obj = null;
    let objType;
    for (let i = 0; i < infoInput.folders.length; i++) {
        if (infoInput.folders[i].address === address) {
            obj = infoInput.folders[i];
            objType = "folders"
            break;
        }
    }
    if (obj === null) {
        for (let i = 0; i < infoInput.files.length; i++) {
            if (infoInput.files[i].address === address) {
                obj = infoInput.files[i];
                objType = "files"
                break;
            }
        }
    }
    document.getElementById('shareModallLabel').textContent = "Share '" + obj.name + "'";

    const table = document.getElementById("tableShare");
    const row = document.getElementById("trPattern");

    const owner = document.getElementById("owner");
    owner.textContent = obj.owner;

    const permissions = Object.entries(obj.permissions);
    if (permissions.length > 0) {
        for (let i = 0; i < permissions.length; i++) {
            const rowCopy = row.cloneNode(true);
            const accessRight = permissions[i][1];
            rowCopy.hidden = false;
            rowCopy.cells[0].textContent = permissions[i][0];
            rowCopy.cells[1].querySelector("button").textContent =
                accessRight.charAt(0).toUpperCase() + accessRight.slice(1).toLowerCase();
            table.appendChild(rowCopy);
        }
    }
    let gar = document.getElementById('globalAccessRight');
    if (obj.openToAll === null) {
        gar.textContent = "Restricted";
    } else {
        gar.textContent = "Anyone with the link";
        const gb = document.getElementById('globalButton');
        gb.textContent = obj.openToAll.charAt(0).toUpperCase() + obj.openToAll.slice(1).toLowerCase();
        gb.hidden = false;
    }
    document.getElementById('copyLink').value =
        (infoInput.domain + "/" + objType + "/" + obj.address + "?link=shared");
}

function showTable() {
    document.getElementById("tableShare").innerHTML = originalTableHTML
    createShareTable();
}


function moveToFolder(address) {
    location.href = '/folders/' + address;
}

function selectRow(row, contextmenuFlag) {
    const renameLi = document.getElementById("rename");
    const shareLi = document.getElementById("share");
    renameLi.style.display = "block";
    shareLi.style.display = "block";
    let checkboxes = document.querySelectorAll('input[type=checkbox]');
    let checkbox = row.querySelector('input[type=checkbox]');
    if (contextmenuFlag === false || checkbox.checked === false) {
        for (let i = 0; i < checkboxes.length; i++) {
            checkboxes[i].checked = false;
        }
    }
    if ($("input:checked").length > 1) {
        renameLi.style.display = "none";
        shareLi.style.display = "none";
    }
    checkbox.checked = true;
}

function toggleAllCheckboxes(toggle) {
    var checkboxes = document.querySelectorAll('table input[type="checkbox"]');
    for (var i = 0; i < checkboxes.length; i++) {
        checkboxes[i].checked = !toggle.checked;
    }
    toggle.checked = !toggle.checked;
}

const pageMenu = document.querySelector("#pageMenu");
const tableMenu = document.querySelector("#tableMenu");
let menuVisible = false;
document.addEventListener("contextmenu", function (event) {
    event.preventDefault();
    const target = event.target;
    const emptySpace = document.querySelector("#dragItem");

    if (target.tagName === "TD" || target.tagName === "LABEL") {
        showTableMenu();
    } else if (target === emptySpace) {
        if (pageMenu != null) {
            showPageMenu();
        }
    }
});

document.addEventListener("click", function (event) {

    if (menuVisible && event.target.tagName != "BUTTON") {
        if (pageMenu != null) {
            pageMenu.style.display = "none";
            pageMenu.style.display = "";
        }
        tableMenu.style.display = "none";
        tableMenu.style.display = "";
        menuVisible = false;
    }
});

function showPageMenu() {
    tableMenu.style.display = "none";
    pageMenu.style.display = "block";

    if (event.clientX > screen.width - 180) {
        pageMenu.style.left = screen.width - 180 + "px";
    } else {
        pageMenu.style.left = event.clientX - 10 + "px";
    }
    if (event.clientY > screen.height - 260) {
        pageMenu.style.top = screen.height - 350 + "px";
    } else {
        pageMenu.style.top = event.clientY - 90 + "px";
    }
    menuVisible = true;
}

function showTableMenu() {
    pageMenu.style.display = "none";
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


