package com.example.gstinvoicegenerator

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.gstinvoicegenerator.components.*
import com.example.gstinvoicegenerator.models.InvoiceData
import com.example.gstinvoicegenerator.models.LineItem
import com.example.gstinvoicegenerator.utils.PDFGenerator
import kotlinx.coroutines.launch

@Composable
fun InvoiceScreen() {
    var invoiceData by remember { mutableStateOf(InvoiceData()) }
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showPreview by remember { mutableStateOf(false) }

    BackHandler {
        showExitConfirmationDialog(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GST Invoice Generator") },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            CustomerForm(
                customerName = invoiceData.customerName,
                address = invoiceData.address,
                mobile = invoiceData.mobile,
                panAadhar = invoiceData.panAadhar,
                gstin = invoiceData.gstin,
                isInterState = invoiceData.isInterState,
                date = invoiceData.date,
                onInputChange = { field, value ->
                    invoiceData = when (field) {
                        "customerName" -> invoiceData.copy(customerName = value as String)
                        "address" -> invoiceData.copy(address = value as String)
                        "mobile" -> invoiceData.copy(mobile = value as String)
                        "panAadhar" -> invoiceData.copy(panAadhar = value as String)
                        "gstin" -> invoiceData.copy(gstin = value as String)
                        "isInterState" -> invoiceData.copy(isInterState = value as Boolean)
                        "date" -> invoiceData.copy(date = value as String)
                        else -> invoiceData
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            VehicleForm(
                vehicleNo = invoiceData.vehicleNo,
                driverName = invoiceData.driverName,
                driverMobile = invoiceData.driverMobile,
                onInputChange = { field, value ->
                    invoiceData = when (field) {
                        "vehicleNo" -> invoiceData.copy(vehicleNo = value as String)
                        "driverName" -> invoiceData.copy(driverName = value as String)
                        "driverMobile" -> invoiceData.copy(driverMobile = value as String)
                        else -> invoiceData
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            LineItemsForm(
                lineItems = invoiceData.lineItems,
                onAddItem = {
                    invoiceData = invoiceData.copy(
                        lineItems = invoiceData.lineItems + LineItem()
                    )
                },
                onRemoveItem = { index ->
                    invoiceData = invoiceData.copy(
                        lineItems = invoiceData.lineItems.filterIndexed { i, _ -> i != index }
                    )
                },
                onUpdateItem = { index, field, value ->
                    invoiceData = invoiceData.copy(
                        lineItems = invoiceData.lineItems.mapIndexed { i, item ->
                            if (i == index) {
                                when (field) {
                                    "particulars" -> item.copy(particulars = value as String)
                                    "hsn" -> item.copy(hsn = value as String)
                                    "quantity" -> item.copy(quantity = value as Int)
                                    "weight" -> item.copy(weight = value as Double)
                                    "rate" -> item.copy(rate = value as Double)
                                    "gstRate" -> item.copy(gstRate = value as Double)
                                    else -> item
                                }
                            } else item
                        }
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            GSTSummary(
                subtotal = invoiceData.lineItems.sumOf { it.amount },
                isInterState = invoiceData.isInterState,
                gst = calculateGST(invoiceData.lineItems)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { resetForm(context) { invoiceData = InvoiceData() } },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Reset")
                }
                Button(
                    onClick = { saveInvoice(context, invoiceData) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Save")
                }
                Button(
                    onClick = { 
                        scope.launch {
                            val pdfFile = PDFGenerator.generateInvoicePDF(context, invoiceData)
                            showPreview = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Text("Generate PDF")
                }
            }
        }
    }

    if (showPreview) {
        AlertDialog(
            onDismissRequest = { showPreview = false },
            title = { Text("PDF Preview") },
            text = { Text("Your PDF has been generated successfully!") },
            confirmButton = {
                Button(onClick = { showPreview = false }) {
                    Text("Close")
                }
            }
        )
    }
}

private fun calculateGST(lineItems: List<LineItem>): Map<String, Double> {
    val totalGST = lineItems.sumOf { it.amount * it.gstRate }
    return mapOf(
        "sgst" to totalGST / 2,
        "cgst" to totalGST / 2,
        "igst" to totalGST
    )
}

private fun showExitConfirmationDialog(context: Context) {
    // Implement exit confirmation dialog
    Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
}

private fun resetForm(context: Context, onReset: () -> Unit) {
    // Implement reset confirmation dialog
    Toast.makeText(context, "Form reset", Toast.LENGTH_SHORT).show()
    onReset()
}

private fun saveInvoice(context: Context, invoiceData: InvoiceData) {
    // Implement save functionality
    Toast.makeText(context, "Invoice saved", Toast.LENGTH_SHORT).show()
}

private fun showPDFPreview(context: Context, pdfFile: java.io.File) {
    // Implement PDF preview functionality
    Toast.makeText(context, "PDF preview: ${pdfFile.absolutePath}", Toast.LENGTH_LONG).show()
}

