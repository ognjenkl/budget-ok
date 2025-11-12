import { Button, Space, Typography, Form, Input, InputNumber as AntdInputNumber, message, Radio, Tooltip, Modal, Table } from 'antd';
import './Expenses.css';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useState, useEffect, useRef } from 'react';
import { PlusOutlined } from '@ant-design/icons';
import type { TransactionType, Envelope } from '../../api/getEnvelopes';

import createExpense from '../../api/createExpense';
import type { CreateExpenseDto } from '../../api/createExpense.dto';
import type { CreateExpenseResponse } from '../../api/createExpense.response';

const { Text, Title } = Typography;

interface ExpenseType {
  id: string;
  amount: number;
  memo: string;
  description?: string;
  date: string;
  transactionType: TransactionType;
}

interface ExpensesProps {
  envelope: Envelope;
  onClose: () => void;
}

export default function Expenses({ envelope, onClose }: ExpensesProps) {
  const [form] = Form.useForm<CreateExpenseDto>();
  // Modal visibility state
  const [modalState, setModalState] = useState({
    isAddExpenseModalVisible: false
  });
  const { isAddExpenseModalVisible } = modalState;
  const amountInputRef = useRef<HTMLInputElement>(null);
  const queryClient = useQueryClient();
  
  // Set default form values
  useEffect(() => {
    form.setFieldsValue({
      transactionType: 'WITHDRAW' as TransactionType
    });
  }, [form]);

  const { mutate: addExpenseMutation, isPending: isCreating } = useMutation<CreateExpenseResponse, Error, CreateExpenseDto>({
    mutationFn: (expenseData: CreateExpenseDto) => createExpense(envelope.id, {
      ...expenseData,
      amount: expenseData.amount * (expenseData.transactionType === 'WITHDRAW' ? -1 : 1)
    }),
    onSuccess: () => {
      message.success('Expense added successfully');
      form.resetFields();
      setModalState(prev => ({ ...prev, isAddExpenseModalVisible: false }));
      queryClient.invalidateQueries({ queryKey: ['envelopes'] });
    },
    onError: (error) => {
      const errorMessage = error?.message || 'Failed to add expense';
      message.error(errorMessage);
    },
  });

  const columns = [
    {
      title: 'Type',
      dataIndex: 'transactionType',
      key: 'transactionType',
      render: (type: TransactionType) => type === 'WITHDRAW' ? 'Withdraw' : 'Deposit',
    },
    {
      title: 'Amount',
      dataIndex: 'amount',
      key: 'amount',
      render: (amount: number, record: ExpenseType) => (
        <span className={record.transactionType === 'WITHDRAW' ? 'amount-withdraw' : 'amount-deposit'}>
          {record.transactionType === 'WITHDRAW' ? '-' : '+'}${amount.toFixed(2)}
        </span>
      ),
    },
    {
      title: 'Memo',
      dataIndex: 'memo',
      key: 'memo',
      render: (memo: string) => memo || '-',
    },
    {
      title: 'Description',
      dataIndex: 'description',
      key: 'description',
      render: (description: string) => description || '-',
    },
    {
      title: 'Date',
      dataIndex: 'date',
      key: 'date',
      render: (date: string) => new Date(date).toLocaleDateString(),
    },
  ];

  const handleAddExpense = async () => {
    try {
      const values = await form.validateFields();
      addExpenseMutation({
        ...values,
        amount: Math.abs(values.amount) // Ensure amount is positive
      });
    } catch (error) {
      console.error('Form validation failed:', error);
    }
  };

  // Focus amount input when modal opens
  useEffect(() => {
    if (isAddExpenseModalVisible && amountInputRef.current) {
      // Small timeout to ensure the modal is fully rendered
      const timer = setTimeout(() => {
        amountInputRef.current?.focus();
      }, 100);
      return () => clearTimeout(timer);
    }
  }, [isAddExpenseModalVisible]);

  const totalExpenses = envelope.expenses?.reduce((sum, expense) => 
    expense.transactionType === 'WITHDRAW' ? sum - expense.amount : sum + expense.amount, 0) || 0;

  return (
    <Modal
      title={`Expenses for ${envelope.name}`}
      open={true}
      onCancel={onClose}
      footer={null}
      width={800}
    >
      <div className="expenses-container">
        <Space direction="vertical" className="expenses-space-container">
          <div className="expenses-header">
            <Title level={5} className="expenses-title">Expense History</Title>
            <Tooltip title="Add Expense">
              <Button
                type="primary"
                icon={<PlusOutlined />}
                onClick={() => setModalState(prev => ({ ...prev, isAddExpenseModalVisible: true }))}
                aria-label="Add Expense"
                className="add-expense-button"
              />
            </Tooltip>
          </div>
          <Text strong>Total Spent: ${totalExpenses.toFixed(2)}</Text>
        </Space>
      </div>

      <Table 
        columns={columns} 
        dataSource={envelope.expenses || []} 
        rowKey="id"
        pagination={{ pageSize: 10 }}
        style={{ marginTop: 16 }}
      />

      <Modal
        title="Add New Expense"
        open={isAddExpenseModalVisible}
        onCancel={() => {
          setModalState(prev => ({ ...prev, isAddExpenseModalVisible: false }));
          form.resetFields();
        }}
        onOk={handleAddExpense}
        okButtonProps={{ loading: isCreating }}
        confirmLoading={isCreating}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="amount"
            label="Amount"
            rules={[{ required: true, message: 'Please enter the amount' }]}
          >
            <AntdInputNumber
              ref={amountInputRef}
              className="amount-input"
              min={0.01}
              step={0.01}
              precision={2}
              prefix="$"
              autoFocus
            />
          </Form.Item>
          <Form.Item
            name="memo"
            label="Memo"
            rules={[{ required: true, message: 'Please enter a memo' }]}
          >
            <Input placeholder="Enter a short note" />
          </Form.Item>
          <Form.Item
            name="description"
            label="Description (Optional)"
          >
            <Input.TextArea rows={2} />
          </Form.Item>
          <Form.Item
            name="transactionType"
            label="Transaction Type"
            rules={[{ required: true }]}
          >
            <Radio.Group>
              <Radio.Button value="WITHDRAW">Withdraw</Radio.Button>
              <Radio.Button value="DEPOSIT">Deposit</Radio.Button>
            </Radio.Group>
          </Form.Item>
        </Form>
      </Modal>
    </Modal>
  );
}
